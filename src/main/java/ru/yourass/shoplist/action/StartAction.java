package ru.yourass.shoplist.action;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.model.Update;
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
    public boolean handle(Update update) {
        try {
            shopListService.saveUser(update.getUpdateId(), update.getMessage().getFrom().getUserName());
            telegramService.sendMessage(update.getMessage().getFrom().getId(), messageSource.getMessage(
                    "message.start",
                    new Object[]{this.getNameFromUpdate(update)},
                    "Юзер",
                    Locale.getDefault()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean callback(Update update) {
        return true;
    }

    private String getNameFromUpdate(Update update) {
        StringBuilder nameBuilder = new StringBuilder();
        String firstName = update.getMessage().getFrom().getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            nameBuilder.append(firstName);
            String lastName = update.getMessage().getFrom().getLastName();
            if (lastName != null && !lastName.isEmpty()) {
                nameBuilder.append(" ").append(lastName);
            }
        } else {
            nameBuilder.append(update.getMessage().getFrom().getUserName());
        }
        return nameBuilder.toString();
    }
}
