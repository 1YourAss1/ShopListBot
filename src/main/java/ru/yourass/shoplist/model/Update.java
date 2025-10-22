package ru.yourass.shoplist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter(AccessLevel.PROTECTED)
public class Update {
    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private Message message;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @JsonProperty("from")
        private User from;

        @JsonProperty("text")
        private String text;
    }
}
