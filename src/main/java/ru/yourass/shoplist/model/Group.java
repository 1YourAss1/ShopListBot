package ru.yourass.shoplist.model;

import lombok.Data;

import java.util.Date;

@Data
public class Group {
    private Long id;
    private User owner;
    private Date createdAt;
}
