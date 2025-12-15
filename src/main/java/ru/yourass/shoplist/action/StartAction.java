package ru.yourass.shoplist.action;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.User;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.services.TelegramService;

import java.util.Locale;

@Slf4j
@Component
public class StartAction implements Action {
    private final ShopListService shopListService;
    private final TelegramService telegramService;
    private final MessageSource messageSource;

    public StartAction(ShopListService shopListService, TelegramService telegramService, MessageSource messageSource) {
        this.shopListService = shopListService;
        this.telegramService = telegramService;
        this.messageSource = messageSource;
    }

    @Override
    public String getKey() {
        return "/start";
    }

    @Override
    public boolean handle(User user, String data) {
        try {
            shopListService.saveUser(user, false);
            telegramService.sendMessage(user.getId(), messageSource.getMessage(
                    "message.start",
                    new Object[]{user.toString()},
                    Locale.getDefault()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean callback(User user, String data) {
        return true;
    }
}
