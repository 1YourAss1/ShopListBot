package ru.yourass.shoplist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    private Long id;
    private User user;
    private Product product;
    private boolean completed;
    private Date createdAt;
    private Date updatedAt;
}