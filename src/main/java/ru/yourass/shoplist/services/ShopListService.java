package ru.yourass.shoplist.services;

import org.springframework.stereotype.Service;
import ru.yourass.shoplist.dao.PurchaseDAO;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Purchase;
import ru.yourass.shoplist.model.User;

import java.util.List;

@Service
public class ShopListService {
    private final UserDAO userDAO;
    private final PurchaseDAO purchaseDAO;

    public ShopListService(UserDAO userDAO, PurchaseDAO purchaseDAO) {
        this.userDAO = userDAO;
        this.purchaseDAO = purchaseDAO;
    }

    public void saveUser(Long userId, String name) {
        User user = new User();
        user.setId(userId);
        user.setUserName(name);
        userDAO.save(user);
    }

    public void savePurchase(User user, String purchaseTitle) {
        Long userId = user.getId();
        if (!userDAO.existsById(userId)) {
            userDAO.save(user);
        }

        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setTitle(purchaseTitle);
        purchaseDAO.save(purchase);
    }

    public List<Purchase> getPurchasesByUserId(Long userId) {
        return purchaseDAO.getByUserId(userId);
    }

    public void togglePurchase(Purchase purchase) {
        purchaseDAO.updateComplete(purchase.getId(), purchase.isCompleted());
    }
}
