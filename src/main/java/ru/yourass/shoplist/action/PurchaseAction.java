package ru.yourass.shoplist.action;

import org.springframework.stereotype.Component;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Update;
import ru.yourass.shoplist.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PurchaseAction implements Action {
    private static final Pattern PATTERN = Pattern.compile("^\\s*(.+?)(?:\\s+(\\d+(?:[.,]\\d+)?))??\\s*$");
    private final ShopListService shopListService;

    public PurchaseAction(ShopListService shopListService) {
        this.shopListService = shopListService;
    }

    @Override
    public String getKey() {
        return "default";
    }

    @Override
    public boolean handle(Update update) {
        User user = update.getMessage().getFrom();
        Matcher matcher = PATTERN.matcher(update.getMessage().getText());
        if (matcher.matches()) {
            String productName = matcher.group(1);
            Float quantity = matcher.group(2) !=  null ? Float.parseFloat(matcher.group(2).replace(',', '.')) : null;
            shopListService.savePurchase(user, productName, quantity);
        }
        // TODO: TelegramService.sendMessageToGroup() to users in group except initiator
        return false;
    }

    @Override
    public boolean callback(Update update) {
        return false;
    }
}
