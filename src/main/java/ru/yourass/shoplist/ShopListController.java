package ru.yourass.shoplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Controller
public class ShopListController {
    private static final Logger logger = LoggerFactory.getLogger(ShopListController.class);
    
    private final RestTemplate restTemplate;
    
    ShopListController(RestTemplate restTemplate) {
       this.restTemplate = restTemplate; 
    }
    
    @Value("${shop.list.bot.token}")
    private String botToken;
    
    @Value("${shop.list.bot.webhook.url}")
    private String webhookUrl;
    
    @PostConstruct
    public void registerWebhook() {
        String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;
        ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, null, String.class);
        if (HttpStatus.OK.equals(response.getStatusCode())) {
            logger.info("Webhook '{}' registrered", webhookUrl);
        } else {
            logger.error("Failed to register webhook: {}", response.getBody());
        }
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String update) {
        logger.info("Get update:\n{}", update);
        return ResponseEntity.ok("ok");
    }
}