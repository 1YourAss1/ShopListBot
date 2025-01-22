package ru.yourass.shoplist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShopListController {
    
    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}