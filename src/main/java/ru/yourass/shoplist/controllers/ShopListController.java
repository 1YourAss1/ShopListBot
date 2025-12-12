package ru.yourass.shoplist.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import ru.yourass.shoplist.action.ActionDispatcher;
import ru.yourass.shoplist.model.Update;
import ru.yourass.shoplist.services.TelegramService;

@Slf4j
@Controller
public class ShopListController {
    private final TelegramService telegramService;
    private final ActionDispatcher actionDispatcher;

    ShopListController(TelegramService telegramService, ActionDispatcher actionDispatcher) {
        this.telegramService = telegramService;
        this.actionDispatcher = actionDispatcher;
    }
    
    @PostConstruct
    public void init() {
        telegramService.registerWebhook();
    }
    
    @PostMapping(value = "/")
    public ResponseEntity<HttpStatus> handleWebhook(@RequestBody Update update) {
        actionDispatcher.dispatch(update);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/")
    public String shopList() {
        return "index";
    }
}