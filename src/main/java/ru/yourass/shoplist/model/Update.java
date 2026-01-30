package ru.yourass.shoplist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter(AccessLevel.PROTECTED)
public class Update {
    @JsonProperty("update_id")
    private Long updateId;

    private Message message;

    @JsonProperty("callback_query")
    private CallbackQuery callbackQuery;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private User from;
        private String text;
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackQuery {
        private User from;
        private String data;
    }
}
