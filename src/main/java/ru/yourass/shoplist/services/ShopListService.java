package ru.yourass.shoplist.services;

import org.springframework.stereotype.Service;
import ru.yourass.shoplist.dao.ProductDAO;
import ru.yourass.shoplist.dao.PurchaseDAO;
import ru.yourass.shoplist.dao.UserDAO;
import ru.yourass.shoplist.model.Product;
import ru.yourass.shoplist.model.Purchase;
import ru.yourass.shoplist.model.User;

import java.util.List;

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

        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setProduct(product);
        purchaseDAO.save(purchase);
    }

    public List<Purchase> getPurchasesByUserId(Long userId, boolean onlyIncompleted) {
        if (onlyIncompleted) {
            return purchaseDAO.getIncompletedByUserId(userId);
        } else {
            return purchaseDAO.getAllByUserId(userId);
        }
    }

    public void togglePurchase(Purchase purchase) {
        purchaseDAO.updateComplete(purchase.getId(), purchase.isCompleted());
    }
}
