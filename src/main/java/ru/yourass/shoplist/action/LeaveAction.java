package ru.yourass.shoplist.action;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.services.TelegramService;

import java.util.Locale;

@Component
public class LeaveAction implements Action {
    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public LeaveAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "/leave";
    }

    @Override
    @Transactional
    public boolean handle(User user, String data) {
        shopListService.getActiveGroupMembersForUser(user).ifPresent(groupMember -> {
            shopListService.setGroupMemberStatus(groupMember, "inactive");
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.leave.yourself",
                    new Object[]{},
                    Locale.getDefault()));

            Group group = groupMember.getGroup();
            if (group.getOwner().equals(user)) {
                shopListService.getActiveGroupMembersForGroup(group).forEach(groupMemberByGroup -> {
                    shopListService.setGroupMemberStatus(groupMemberByGroup, "inactive");
                    User userInGroup = groupMemberByGroup.getUser();
                    telegramService.sendMessage(userInGroup.getId(), messageSource.getMessage(
                            "message.leave.kick",
                            new Object[]{user.toString()},
                            Locale.getDefault()));
                });
            }
        });
        return false;
    }

    @Override
    public boolean callback(User user, String data) {
        return false;
    }
}
