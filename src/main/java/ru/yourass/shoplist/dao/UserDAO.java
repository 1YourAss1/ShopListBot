package ru.yourass.shoplist.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class UserDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = :userId";
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        sql,
                        new MapSqlParameterSource("userId", id),
                        Integer.class
                )
        ).map(count -> count > 0).orElse(false);
    }

    public void save(User user) {
        String sql = """
            INSERT INTO users (id, username, first_name, last_name, created_at)
            VALUES (:id, :userName, :firstName, :lastName, :createdAt)
            ON CONFLICT (id) DO UPDATE
                SET username = :userName, first_name = :firstName, last_name = :lastName
           """;
        jdbcTemplate.update(sql,
                new MapSqlParameterSource()
                        .addValue("id", user.getId())
                        .addValue("userName", user.getUserName())
                        .addValue("firstName", user.getFirstName())
                        .addValue("lastName", user.getLastName())
                        .addValue("createdAt", LocalDateTime.now()));
    }

    public Optional<User> findById(Long id) {
        String sql = """
                SELECT * FROM users WHERE id = :userId
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    new MapSqlParameterSource("userId", id),
                    new BeanPropertyRowMapper<>(User.class))
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
