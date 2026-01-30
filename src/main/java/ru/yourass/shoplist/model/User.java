package ru.yourass.shoplist.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class User {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String userName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private Date createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder nameBuilder = new StringBuilder();
        if (this.firstName != null && !this.firstName.isEmpty()) {
            nameBuilder.append(firstName);
            if (this.lastName != null && !this.lastName.isEmpty()) {
                nameBuilder.append(" ").append(lastName);
            }
        } else if (this.userName != null && !this.userName.isEmpty()) {
            nameBuilder.append(this.userName);
        } else {
            nameBuilder.append(this.id.toString());
        }
        return nameBuilder.toString();
    }
}
