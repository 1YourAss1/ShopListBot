package ru.yourass.shoplist.dao;

import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.Purchase;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PurchaseDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PurchaseDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Purchase purchase) {
        String sql = """
            INSERT INTO purchases (user_id, title, completed, added_at)\s
            VALUES (:userId, :title, :completed, :addedAt)
           """;
        jdbcTemplate.update(sql,
                new MapSqlParameterSource()
                        .addValue("userId", purchase.getUserId())
                        .addValue("title", purchase.getTitle())
                        .addValue("completed", purchase.isCompleted())
                        .addValue("addedAt", LocalDateTime.now()));
    }

    public List<Purchase> getByUserId(Long userId) {
        String sql = """
            SELECT * FROM purchases WHERE user_id = :userId;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                new BeanPropertyRowMapper<>(Purchase.class));
    }

    public void updateComplete(Long purchaseId, boolean complete) {
        String sql = """
            UPDATE purchases SET completed = :completed WHERE id = :id
        """;
        var params = new MapSqlParameterSource()
                .addValue("completed", complete)
                .addValue("id", purchaseId);
        int rows = jdbcTemplate.update(sql, params);
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, rows);
        }
    }
}