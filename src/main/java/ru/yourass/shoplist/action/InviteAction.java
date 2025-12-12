package ru.yourass.shoplist.action;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.exceptions.GroupException;
import ru.yourass.shoplist.exceptions.InvitationException;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.services.TelegramService;

import java.util.*;

@Component
public class InviteAction implements Action {
    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public InviteAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "/invite";
    }

    @Override
    public boolean handle(User user, String data) {
        telegramService.sendMessage(user.getId(), messageSource.getMessage(
                "message.invite",
                new Object[]{user.getId().toString()},
                Locale.getDefault()));

        return true;
    }

    @Override
    public boolean callback(User user, String data) {
        try {
            Long userId = Long.parseLong(data.trim());
            Optional<User> optUser = shopListService.getUser(userId);
            if (optUser.isEmpty()) {
                telegramService.sendMessage(user.getId(), messageSource.getMessage(
                        "message.user-not-found",
                        new Object[]{userId.toString()},
                        Locale.getDefault()));
                return true;
            }

            User userToInvite = optUser.get();
            Group group = shopListService.getUserGroup(user);

            shopListService.inviteUserToOwnerGroup(userToInvite, user, group);

            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.for-owner.success-invited",
                    new Object[]{userToInvite.toString()},
                    Locale.getDefault()));

            Map<String, String> inlineButtonsRow = new LinkedHashMap<>();
            inlineButtonsRow.put("✅ Принять", "/inviteResponse accept " + group.getId());
            inlineButtonsRow.put("❌ Отклонить", "/inviteResponse decline " + group.getId());
            telegramService.sendMessage(userToInvite.getId(), messageSource.getMessage(
                    "message.for-user.success-invited",
                    new Object[]{user.toString()},
                    Locale.getDefault()),
                    List.of(inlineButtonsRow));
            return true;
        } catch (NumberFormatException e) {
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.invalid-id-format",
                    new Object[]{},
                    Locale.getDefault()));
            return false;
        } catch (GroupException e) {
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.group-not-found",
                    new Object[]{},
                    Locale.getDefault()));
            return true;
        } catch (InvitationException e) {
            telegramService.sendMessage(user.getId(), e.getMessage());
            return true;
        }
    }
}
