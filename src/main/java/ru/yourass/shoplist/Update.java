package ru.yourass.shoplist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Update {
    @JsonProperty("update_id")
    private Integer updateId;

    @JsonProperty("message")
    private Message message;

    @Getter
    @Setter
    static class Message {
        @JsonProperty("text")
        private String text;
    }
}
