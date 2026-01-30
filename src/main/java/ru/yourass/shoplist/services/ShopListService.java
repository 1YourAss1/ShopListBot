package ru.yourass.shoplist.services;

import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yourass.shoplist.dao.*;
import ru.yourass.shoplist.exceptions.GroupMemberException;
import ru.yourass.shoplist.exceptions.GroupException;
import ru.yourass.shoplist.exceptions.InvitationException;
import ru.yourass.shoplist.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopListService {
    private static final Logger logger = LoggerFactory.getLogger(ShopListService.class);
    private static final String INVITED = "invited";
    private static final String ACTIVE = "active";
    private static final String INACTIVE = "inactive";

    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;

    public ShopListService(UserDAO userDAO, ProductDAO productDAO, PurchaseDAO purchaseDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO) {
        this.userDAO = userDAO;
        this.productDAO = productDAO;
        this.purchaseDAO = purchaseDAO;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;
    }

    @Transactional
    public void createUsersDbIfNotExists(User user) {
        this.saveUser(user, true);
        Group group = this.getOrCreateGroupForUser(user);
        this.saveGroupMemberIfNotExists(group, user);
    }

    public void saveUser(User user, boolean checkExists) {
        if (checkExists && userDAO.existsById(user.getId())) {
            return;
        }
        userDAO.save(user);
    }

    private Group getOrCreateGroupForUser(User user) {
        Optional<Group> optGroup = groupDAO.getByUser(user);
        if (optGroup.isPresent()) {
            return optGroup.get();
        } else {
            Group group = new Group();
            group.setOwner(user);
            group.setId(groupDAO.save(group));
            return group;
        }
    }

    private void saveGroupMemberIfNotExists(Group group, User user) {
        Optional<GroupMember> optGroupMember = groupMemberDAO.getByGroupAndUser(group, user);
        if (optGroupMember.isEmpty()) {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroup(group);
            groupMember.setUser(user);
            groupMember.setStatus(INACTIVE);
            groupMemberDAO.save(groupMember);
        }
    }

    public Optional<User> getUser(Long userId) {
        return userDAO.findById(userId);
    }

    public void savePurchase(User user, String productName, @Nullable Float quantity) {
        Product product = productDAO.getByName(productName);
        Optional<Purchase> optPurchase = purchaseDAO.getByProductForUser(product, user);
        if (optPurchase.isPresent()) {
            Purchase purchase = optPurchase.get();
            purchase.setQuantity(quantity);
            if (purchase.isCompleted()) {
                purchase.setCompleted(false);
            }
            purchaseDAO.update(purchase);
        } else {
            Purchase purchase = new Purchase();
            purchase.setUser(user);
            purchase.setProduct(product);
            purchase.setQuantity(quantity);
            purchaseDAO.save(purchase);
        }
    }

    /***
     * Возвращает все незавершенные покупки и 10 последних завершенных
     * @param userId Телеграм идентификатор пользователя
     * @return список покупок для группы в которой находится пользователь
     */
    public List<Purchase> getPurchasesByUserId(Long userId) {
        List<Purchase> purchases = new ArrayList<>();

        Optional<GroupMember> userActiveGroupMember = groupMemberDAO.getActiveByUserId(userId);
        if (userActiveGroupMember.isPresent()) {
            Group group = userActiveGroupMember.get().getGroup();
            groupMemberDAO.getActiveByGroup(group).forEach(groupMemberForGroup -> {
                User userForGroup = groupMemberForGroup.getUser();
                purchases.addAll(purchaseDAO.getIncompletedByUserId(userForGroup.getId()));
                purchases.addAll(purchaseDAO.getCompletedByUserId(userForGroup.getId(), 10));
            });
        } else {
            purchases.addAll(purchaseDAO.getIncompletedByUserId(userId));
            purchases.addAll(purchaseDAO.getCompletedByUserId(userId, 10));
        }

        purchases.sort(Comparator.comparing(Purchase::getUpdatedAt).reversed());
        return purchases;
    }

    public void updatePurchase(Purchase purchase) {
        purchaseDAO.update(purchase);
    }

    public Group getUserGroup(User user) throws GroupException {
        Optional<Group> optOwnerGroup = groupDAO.getByUser(user);
        if (optOwnerGroup.isEmpty()) {
            throw new GroupException("not found group for user");
        }
        return optOwnerGroup.get();
    }

    @Transactional
    public void inviteUserToOwnerGroup(User user, User owner, Group group) throws InvitationException {
        logger.debug("Inviting user {} to owner group {}", user, owner);
        this.checkOwnerInvitedToAnotherGroup(owner);
        this.checkUserInvitedToAnotherGroup(user, owner);
        this.checkUserInvitedToOwnerGroup(user, owner);

        this.updateOrCreateGroupMember(group, user, INVITED);
        this.updateOrCreateGroupMember(group, owner, ACTIVE);
    }

    private void checkOwnerInvitedToAnotherGroup(User owner) throws InvitationException {
        if (groupMemberDAO.getByUser(owner).stream()
                .filter(groupMember -> !owner.equals(groupMember.getGroup().getOwner()) && owner.equals(groupMember.getUser()))
                .anyMatch(groupMember -> !INACTIVE.equals(groupMember.getStatus()))) {
            throw new InvitationException("Вы уже находитесь в другой группе\\. Чтобы вы смогли отправить ему приглашение, вы должны выйти из всех других групп\\.");
        }
    }

    private void checkUserInvitedToAnotherGroup(User user, User owner) throws InvitationException {
        if (groupMemberDAO.getByUser(user).stream()
                .filter(groupMember -> !owner.equals(groupMember.getGroup().getOwner()) && user.equals(groupMember.getUser()))
                .anyMatch(groupMember -> !INACTIVE.equals(groupMember.getStatus()))) {
            throw new InvitationException("Пользователь приглашен или уже принял приглашение в другую группу\\. Чтобы вы смогли отправить ему приглашение, он должен выйти из всех групп\\.");
        }
    }

    private void checkUserInvitedToOwnerGroup(User user, User owner) throws InvitationException {
        if (groupMemberDAO.getByUser(user).stream()
                .filter(groupMember -> owner.equals(groupMember.getGroup().getOwner()) && user.equals(groupMember.getUser()))
                .anyMatch(groupMember -> !INACTIVE.equals(groupMember.getStatus()))) {
            throw new InvitationException("Пользователь приглашен в вашу группу\\.");
        }
    }

    private void updateOrCreateGroupMember(Group group, User user, String status) {
        Optional<GroupMember> optGroupMember = groupMemberDAO.getByGroupAndUser(group, user);
        if (optGroupMember.isPresent()) {
            GroupMember groupMember = optGroupMember.get();
            groupMember.setStatus(status);
            groupMemberDAO.updateStatus(groupMember);
        } else {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroup(group);
            groupMember.setUser(user);
            groupMember.setStatus(status);
            groupMemberDAO.save(groupMember);
        }
    }

    public boolean handleInviteResponse(User user, Long groupId, String response) throws GroupException, GroupMemberException, InvitationException {
        Optional<Group> group = groupDAO.findById(groupId);
        if (group.isEmpty()) {
            throw new GroupException("not found group with id {0}", groupId.toString());
        }

        Optional<GroupMember> optGroupMember = groupMemberDAO.getByGroupAndUser(group.get(), user);
        if (optGroupMember.isEmpty()) {
            throw new GroupMemberException("not found group member by group {0} and user {1}", groupId.toString(), user.getId());
        }

        GroupMember groupMember = optGroupMember.get();
        switch (groupMember.getStatus()) {
            case INACTIVE:
                throw new InvitationException("Вы не приглашены в эту группу\\.");
            case ACTIVE:
                throw new InvitationException("Вы уже находитесь в этой группе\\.");
            case INVITED:
                if ("accept".equals(response)) {
                    this.setGroupMemberStatus(groupMember, ACTIVE);
                    return true;
                } else if ("decline".equals(response)) {
                    this.setGroupMemberStatus(groupMember, INACTIVE);
                    return false;
                }
                throw new InvitationException("Не удалось обработать ответ на приглашение\\.");
            default:
                throw new GroupMemberException("unexpected group member status: " + groupMember.getStatus());
        }
    }

    public void setGroupMemberStatus(GroupMember groupMember, String status) {
        groupMember.setStatus(status);
        groupMemberDAO.updateStatus(groupMember);
    }

    public Optional<Long> getGroupOwnerByGroupId(Long groupId) {
        Optional<Group> group = groupDAO.findById(groupId);
        return group.map(value -> value.getOwner().getId());
    }


    public Optional<GroupMember> getActiveGroupMembersForUser(User user) {
        return groupMemberDAO.getActiveByUserId(user.getId());
    }

    public List<GroupMember> getActiveGroupMembersForGroup(Group group) {
        return groupMemberDAO.getActiveByGroup(group);
    }

    public Optional<String> getGroupStatusString(User user) {
        StringBuilder result = new StringBuilder();
        Optional<GroupMember> activeByUserId = groupMemberDAO.getActiveByUserId(user.getId());
        if (activeByUserId.isPresent()) {
            Group group = activeByUserId.get().getGroup();
            User groupOwner = activeByUserId.get().getGroup().getOwner();
            result.append("\uD83D\uDD38 ").append(groupOwner.toString()).append(" \\(владелец\\)");
            List<GroupMember> activeByGroup = groupMemberDAO.getActiveByGroup(group);
            if (!activeByGroup.isEmpty()) {
                result.append("\n");
                result.append(activeByGroup.stream()
                        .filter(groupMember -> !groupOwner.equals(groupMember.getUser()))
                        .map(groupMember -> "\uD83D\uDD38 " + groupMember.getUser().toString())
                        .collect(Collectors.joining("\n")));
            }
            return Optional.of(result.toString());
        } else {
            return Optional.empty();
        }
    }
}
