package ru.yourass.shoplist.action;

import org.springframework.stereotype.Component;
import ru.yourass.shoplist.services.ShopListService;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Update;
import ru.yourass.shoplist.model.User;

@Component
public class PurchaseAction implements Action {
    private final ShopListService shopListService;

    public PurchaseAction(ShopListService shopListService, UserDAO userDAO) {
        this.shopListService = shopListService;
    }

    @Override
    public String getKey() {
        return "default";
    }

    @Override
    public boolean handle(Update update) {
        User user = update.getMessage().getFrom();
        String productName = update.getMessage().getText();
        shopListService.savePurchase(user, productName);
        // TODO: TelegramService.sendMessageToGroup() to users in group except initiator
        return false;
    }

    @Override
    public boolean callback(Update update) {
        return false;
    }
}
