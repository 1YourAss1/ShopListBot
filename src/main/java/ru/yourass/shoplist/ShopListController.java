package ru.yourass.shoplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.annotation.PostConstruct;

@Controller
public class ShopListController {
    private static final Logger logger = LoggerFactory.getLogger(ShopListController.class);
    
    @PostConstruct
    public void init() {
        logger.info("init");
    }
    
    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}