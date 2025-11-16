package ru.yourass.shoplist.model;

import lombok.Data;

import java.util.Date;

@Data
public class Product {
    private Long id;
    private String name;
    private Date createdAt;
}
