package ru.yourass.shoplist.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TelegramService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    private static final Duration MAX_AGE = Duration.ofMinutes(10);
    private final String apiUrl;
    private final String botToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${bot.webhook.url}")
    private String webhookUrl;

    public TelegramService(RestTemplate restTemplate, @Value("${bot.token}") String botToken) {
        this.restTemplate = restTemplate;
        this.botToken = botToken;
        this.apiUrl = "https://api.telegram.org/bot" + this.botToken;
    }

    public void registerWebhook() {
        String telegramApiUrl = apiUrl + "/setWebhook?url=" + webhookUrl;
        ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, null, String.class);
        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            throw new ResponseStatusException(response.getStatusCode(), response.getBody());
        }
    }

    private void exchangeHttpEntity(URI url, HttpEntity<String> requestEntity) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            logger.debug(response.getBody());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void sendMessage(long chatId, String text) {
        this.sendMessage(chatId, text, Collections.emptyList());
    }

    public void sendMessage(long chatId, String text, List<Map<String, String>> inlineButtons) {
        URI url = URI.create(apiUrl + "/sendMessage");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "";
        try {
            var request = new HashMap<>();
            request.put("chat_id", String.valueOf(chatId));
            request.put("text", text);
            request.put("parse_mode", "MarkdownV2");
            if (!inlineButtons.isEmpty()) {
                request.put("reply_markup", this.getInlineKeyboardMarkup(inlineButtons));
            }
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        this.exchangeHttpEntity(url, new HttpEntity<>(jsonBody, headers));
    }

    private record InlineKeyboardMarkup(@JsonProperty("inline_keyboard") List<List<InlineKeyboardButton>> inlineKeyboard) {}

    private record InlineKeyboardButton(String text, @JsonProperty("callback_data") String callbackData) {}

    private InlineKeyboardMarkup getInlineKeyboardMarkup(List<Map<String, String>> inlineButtons) {
        List<List<InlineKeyboardButton>> inlineKeyboard =  new ArrayList<>();
        inlineButtons.forEach((button) -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            button.forEach((key, value) -> {
                row.add(new InlineKeyboardButton(key, value));
            });
            inlineKeyboard.add(row);
        });
        return new InlineKeyboardMarkup(inlineKeyboard);
    }

    public void setCommands() {
        URI url = URI.create(apiUrl + "/setMyCommands");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = "";
        try {
            var request = new BotCommands(List.of(
                    new BotCommand("/status", "Статус группы"),
                    new BotCommand("/invite", "Пригласить в группу"),
                    new BotCommand("/leave", "Покинуть группу")
            ), "ru");
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        this.exchangeHttpEntity(url, new HttpEntity<>(jsonBody, headers));
    }

    private record BotCommand(String command, String description) {}

    private record BotCommands(List<BotCommand> commands, @JsonProperty("language_code") String languageCode) {}

    public void validateInitData(String initDataHeader) {
        if (initDataHeader == null || initDataHeader.isBlank() || !initDataHeader.startsWith("tma ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        Map<String, String> parsedData = parseInitData(initDataHeader.substring(4));

        String receivedHash = parsedData.get("hash");
        if (receivedHash == null) {
            throw new SecurityException("Hash not found in initData");
        }

        String authDate = parsedData.get("auth_date");
        if (authDate == null) {
            throw new SecurityException("Auth_date not found in initData");
        } else if (!isAuthDateValid(authDate)) {
            throw new SecurityException("InitData is expired");
        }

        if (!validateSignature(parsedData, receivedHash)) {
            throw new SecurityException("Invalid signature");
        }

        String userJsonString = parsedData.get("user");
        if (userJsonString == null || userJsonString.isBlank()) {
            throw new IllegalArgumentException("Missing 'user' field");
        }
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();

        for (String pair : initData.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private boolean isAuthDateValid(String authDateStr) {
        long authEpoch = Long.parseLong(authDateStr);
        Instant authInstant = Instant.ofEpochSecond(authEpoch);
        return !Instant.now().isAfter(authInstant.plus(MAX_AGE));
    }

    private boolean validateSignature(Map<String, String> parsedData, String receivedHash) {
        try {
            String dataCheckString = parsedData.entrySet().stream()
                    .filter(e -> !e.getKey().equals("hash"))
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));

            byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8));
            byte[] calculatedHash = hmacSha256(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
            return hex(calculatedHash).equalsIgnoreCase(receivedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hmacSha256(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
