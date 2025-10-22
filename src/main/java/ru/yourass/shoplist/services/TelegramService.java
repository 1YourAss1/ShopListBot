package ru.yourass.shoplist.services;

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

    private final RestTemplate restTemplate;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.webhook.url}")
    private String webhookUrl;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendMessage(long chatId, String text) {
        URI url = URI.create("https://api.telegram.org/bot" + botToken + "/sendMessage");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text
        );

        ResponseEntity<String> resp = restTemplate.postForEntity(
                url,
                new HttpEntity<>(body, headers),
                String.class
        );

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Telegram API error: HTTP " +
                    resp.getStatusCode().value() + " — " + resp.getBody());
        }
    }

    public void registerWebhook() {
        String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;
        ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, null, String.class);
        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            throw new ResponseStatusException(response.getStatusCode(), response.getBody());
        }
    }

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
