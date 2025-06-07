package ru.yourass.shoplist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class Update {
    @JsonProperty("update_id")
    private Integer updateId;

    @JsonProperty("message")
    private Message message;

    public Optional<String> getText() {
        return Optional.ofNullable(message).map(Message::getText);
    }

    @Getter
    @Setter
    private static class Message {
        @JsonProperty("text")
        private String text;
    }
}
