package ru.yourass.shoplist.model;

import lombok.Data;

import java.util.Date;

@Data
public class GroupMember {
    private Group group;
    private User user;
    private String status;
    private Date createdAt;
}
