package ru.yourass.shoplist.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yourass.shoplist.model.Purchase;
import ru.yourass.shoplist.security.RequireInitData;
import ru.yourass.shoplist.services.ShopListService;

import java.util.List;

@Controller
@RequestMapping("api")
@RequireInitData
public class PurchaseApiController {
    private final ShopListService shopListService;

    public PurchaseApiController(ShopListService shopListService) {
        this.shopListService = shopListService;
    }

    @GetMapping(path = "/purchases/{userId}")
    public ResponseEntity<List<Purchase>> getPurchases(@PathVariable("userId") Long userId) {
        try {
            return ResponseEntity.ok(shopListService.getPurchasesByUserId(userId));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", e);
        }
    }

    @PatchMapping(path = "/toggle", consumes = "application/json")
    public ResponseEntity<HttpStatus> updatePurchase(@RequestBody Purchase purchase) {
        try {
            shopListService.updatePurchase(purchase);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", e);
        }
    }
}
