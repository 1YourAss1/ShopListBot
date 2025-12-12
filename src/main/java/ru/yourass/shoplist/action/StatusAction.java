package ru.yourass.shoplist.action;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.services.TelegramService;

import java.util.Locale;
import java.util.Optional;

@Component
public class StatusAction implements Action {
    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public StatusAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "/status";
    }

    @Override
    public boolean handle(User user, String data) {
        Optional<String> groupStatusString = shopListService.getGroupStatusString(user);
        if (groupStatusString.isPresent()) {
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.status.group",
                    new Object[]{groupStatusString.get()},
                    Locale.getDefault()));
        } else {
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.status.empty",
                    new Object[]{},
                    Locale.getDefault()));
        }
        return false;
    }

    @Override
    public boolean callback(User user, String data) {
        return false;
    }
}
