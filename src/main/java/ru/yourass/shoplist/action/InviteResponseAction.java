package ru.yourass.shoplist.action;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.exceptions.GroupMemberException;
import ru.yourass.shoplist.exceptions.GroupException;
import ru.yourass.shoplist.exceptions.InvitationException;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.services.TelegramService;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InviteResponseAction implements Action {
    private static final Pattern INVITE_RESPONSE_PATTERN = Pattern.compile("^(\\w+)\\s*(\\d+)$");

    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public InviteResponseAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "/inviteResponse";
    }

    @Override
    public boolean handle(User user, String data) {
        Matcher matcher = INVITE_RESPONSE_PATTERN.matcher(data);
        if (matcher.matches()) {
            String response = matcher.group(1);
            String groupId = matcher.group(2);
            if (response == null || groupId == null) {
                this.sendErrorMessage(user);
                return false;
            }

            boolean accepted;
            try {
                accepted = shopListService.handleInviteResponse(user, Long.valueOf(groupId), response);
            } catch (GroupException | GroupMemberException e) {
                this.sendErrorMessage(user);
                return false;
            } catch (InvitationException e) {
                telegramService.sendMessage(user.getId(), e.getMessage());
                return false;
            }

            Optional<Long> ownerId = shopListService.getGroupOwnerByGroupId(Long.valueOf(groupId));
            if (accepted) {
                telegramService.sendMessage(user.getId(), messageSource.getMessage(
                        "message.invite-response.accepted",
                        new Object[]{},
                        Locale.getDefault()));
                ownerId.ifPresent(id -> telegramService.sendMessage(id, messageSource.getMessage(
                        "message.invite.accepted",
                        new Object[]{user.toString()},
                        Locale.getDefault())));
            } else {
                telegramService.sendMessage(user.getId(), messageSource.getMessage(
                        "message.invite-response.declined",
                        new Object[]{},
                        Locale.getDefault()));
                ownerId.ifPresent(id -> telegramService.sendMessage(id, messageSource.getMessage(
                        "message.invite.declined",
                        new Object[]{user.toString()},
                        Locale.getDefault())));
            }


        } else {
            this.sendErrorMessage(user);
            return false;
        }
        return false;
    }

    @Override
    public boolean callback(User user, String data) {
        return false;
    }

    private void sendErrorMessage(User user) {
        telegramService.sendMessage(user.getId(), messageSource.getMessage(
                "message.invite-response.error",
                new Object[]{},
                Locale.getDefault()));
    }
}
