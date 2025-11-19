package ru.yourass.shoplist.services;

import org.springframework.stereotype.Service;
import ru.yourass.shoplist.dao.ProductDAO;
import ru.yourass.shoplist.dao.PurchaseDAO;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Product;
import ru.yourass.shoplist.model.Purchase;
import ru.yourass.shoplist.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class ShopListService {
    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final PurchaseDAO purchaseDAO;

    public ShopListService(UserDAO userDAO, ProductDAO productDAO, PurchaseDAO purchaseDAO) {
        this.userDAO = userDAO;
        this.productDAO = productDAO;
        this.purchaseDAO = purchaseDAO;
    }

    public void saveUser(Long userId, String name) {
        User user = new User();
        user.setId(userId);
        user.setUserName(name);
        userDAO.save(user);
    }

    public void savePurchase(User user, String productName) {
        if (!userDAO.existsById(user.getId())) {
            userDAO.save(user);
        }

        Product product = productDAO.getByName(productName);
        Optional<Purchase> optPurchase = purchaseDAO.getByProductForUser(product, user);

        if (optPurchase.isPresent()) {
            Purchase purchase = optPurchase.get();
            if (purchase.isCompleted()) {
                purchaseDAO.updateComplete(purchase, false);
            }
        } else {
            Purchase purchase = new Purchase();
            purchase.setUser(user);
            purchase.setProduct(product);
            purchaseDAO.save(purchase);
        }
    }

    /***
     * Возвращает все незавершенные покупки и 10 последних завершенныйх
     * @param userId Телеграм идентификатор пользователя
     * @return список покупок для пользовал
     */
    public List<Purchase> getPurchasesByUserId(Long userId) {
        List<Purchase> purchases = purchaseDAO.getIncompletedByUserId(userId);
        purchases.addAll(purchaseDAO.getCompletedByUserId(userId, 10));
        return purchases;
    }

    public void togglePurchase(Purchase purchase) {
        purchaseDAO.updateComplete(purchase, purchase.isCompleted());
    }
}
