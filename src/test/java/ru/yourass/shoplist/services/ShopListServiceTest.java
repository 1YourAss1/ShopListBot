package ru.yourass.shoplist.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yourass.shoplist.dao.GroupMemberDAO;
import ru.yourass.shoplist.exceptions.InvitationException;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.GroupMember;
import ru.yourass.shoplist.model.User;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopListServiceTest {

    @Mock
    private GroupMemberDAO groupMemberDAO;
    @InjectMocks
    private ShopListService shopListService;

    private User user;
    private User owner;
    private User otherUser;
    private Group userGroup;
    private Group ownerGroup;
    private Group otherGroup;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        owner = new User();
        owner.setId(2L);
        otherUser = new User();
        otherUser.setId(3L);

        userGroup = new Group();
        userGroup.setOwner(user);
        ownerGroup = new Group();
        ownerGroup.setOwner(owner);
        otherGroup = new Group();
        otherGroup.setOwner(otherUser);
    }

    @Test
    @DisplayName("Владелец приглашен в другую группу")
    void inviteUserToOwnerGroup_ownerInvitedToAnotherGroup_ThrowsException() {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(otherGroup);
        groupMember.setUser(owner);
        groupMember.setStatus("active");

        when(groupMemberDAO.getByUser(owner)).thenReturn(List.of(groupMember));

        InvitationException exception = assertThrows(
                InvitationException.class,
                () -> shopListService.inviteUserToOwnerGroup(user, owner, ownerGroup));

        assertEquals(
                "Вы уже находитесь в другой группе\\. Чтобы вы смогли отправить ему приглашение, вы должны выйти из всех других групп\\.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Пользователь приглашен в другую группу")
    void inviteUserToOwnerGroup_userInvitedToAnotherGroup_ThrowsException() {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(otherGroup);
        groupMember.setUser(user);
        groupMember.setStatus("active");

        when(groupMemberDAO.getByUser(owner)).thenReturn(Collections.emptyList());
        when(groupMemberDAO.getByUser(user)).thenReturn(List.of(groupMember));

        InvitationException exception = assertThrows(
                InvitationException.class,
                () -> shopListService.inviteUserToOwnerGroup(user, owner, ownerGroup));

        assertEquals(
                "Пользователь приглашен или уже принял приглашение в другую группу\\. Чтобы вы смогли отправить ему приглашение, он должен выйти из всех групп\\.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Пользователь приглашен в группу владельца")
    void inviteUserToOwnerGroup_userInvitedToOwnerGroup_ThrowsException() {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(ownerGroup);
        groupMember.setUser(user);
        groupMember.setStatus("active");

        when(groupMemberDAO.getByUser(owner)).thenReturn(Collections.emptyList());
        when(groupMemberDAO.getByUser(user)).thenReturn(List.of(groupMember));

        InvitationException exception = assertThrows(
                InvitationException.class,
                () -> shopListService.inviteUserToOwnerGroup(user, owner, ownerGroup));

        assertEquals(
                "Пользователь приглашен в вашу группу\\.",
                exception.getMessage());
    }
}
