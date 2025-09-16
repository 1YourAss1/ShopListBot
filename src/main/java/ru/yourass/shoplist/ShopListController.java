package ru.yourass.shoplist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Controller
public class ShopListController {
    private static final Logger logger = LoggerFactory.getLogger(ShopListController.class);

    private final ItemRepository itemRepository = new ItemRepository();
    
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
            logger.info("Webhook '{}' registered", webhookUrl);
        } else {
            logger.error("Failed to register webhook: {}", response.getBody());
        }
    }
    
    @PostMapping(value = "/webhook")
    public ResponseEntity<HttpStatus> handleWebhook(@RequestBody Update update) {
        int id = itemRepository.getAll().size() + 1;
        itemRepository.save(new ItemDto(String.valueOf(id), update.getMessage().getText(), false));
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/")
    public String shopList(Model model) {
        model.addAttribute("items", itemRepository.getAll());
        return "shop-list";
    }
}