package ru.yourass.shoplist.dao;

import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.Product;
import ru.yourass.shoplist.model.Purchase;
import ru.yourass.shoplist.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class PurchaseDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PurchaseDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Purchase purchase) {
        Objects.requireNonNull(purchase);
        Objects.requireNonNull(purchase.getUser());
        Objects.requireNonNull(purchase.getProduct());

        String sql = """
            INSERT INTO purchases (user_id, product_id, completed, created_at)\s
            VALUES (:userId, :productId, :completed, :createdAt)
           """;
        jdbcTemplate.update(sql,
                new MapSqlParameterSource()
                        .addValue("userId", purchase.getUser().getId())
                        .addValue("productId", purchase.getProduct().getId())
                        .addValue("completed", purchase.isCompleted())
                        .addValue("createdAt", LocalDateTime.now()));
    }

    public List<Purchase> getAllByUserId(Long userId) {
        String sql = """
                        SELECT
                            purchases.id as "id",
                            purchases.completed as "completed",
                            purchases.created_at as "created_at",
                            purchases.updated_at as "updated_at",
                            users.id as "user_id",
                            users.username as "user_name",
                            users.first_name as "user_first_name",
                            users.last_name as "user_last_name",
                            users.created_at as "user_created_at",
                            products.id as "product_id",
                            products.name as "product_name",
                            products.created_at as "product_createdAt"
                        FROM purchases
                        INNER JOIN users ON users.id = purchases.user_id
                        INNER JOIN products ON purchases.product_id = products.id
                        WHERE purchases.user_id = :userId
                        ORDER BY purchases.created_at DESC;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                new PurchaseProductRowMapper());
    }

    public List<Purchase> getIncompletedByUserId(Long userId) {
        String sql = """
                        SELECT
                            purchases.id as "id",
                            purchases.completed as "completed",
                            purchases.created_at as "created_at",
                            purchases.updated_at as "updated_at",
                            users.id as "user_id",
                            users.username as "user_name",
                            users.first_name as "user_first_name",
                            users.last_name as "user_last_name",
                            users.created_at as "user_created_at",
                            products.id as "product_id",
                            products.name as "product_name",
                            products.created_at as "product_createdAt"
                        FROM purchases
                        INNER JOIN users ON users.id = purchases.user_id
                        INNER JOIN products ON purchases.product_id = products.id
                        WHERE purchases.user_id = :userId AND completed = false
                        ORDER BY purchases.created_at DESC;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("userId", userId),
                new PurchaseProductRowMapper());
    }

    public void updateComplete(Long purchaseId, boolean complete) {
        String sql = """
            UPDATE purchases
            SET completed = :completed, updated_at = :updated_at
            WHERE id = :id
        """;
        var params = new MapSqlParameterSource()
                .addValue("completed", complete)
                .addValue("updated_at", LocalDateTime.now())
                .addValue("id", purchaseId);
        int rows = jdbcTemplate.update(sql, params);
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, rows);
        }
    }

    private static class PurchaseProductRowMapper implements RowMapper<Purchase> {
        @Override
        public Purchase mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setFirstName(rs.getString("user_first_name"));
            user.setLastName(rs.getString("user_last_name"));
            user.setCreatedAt(rs.getTimestamp("user_created_at"));

            Product product = new Product();
            product.setId(rs.getLong("product_id"));
            product.setName(rs.getString("product_name"));
            product.setCreatedAt(rs.getTimestamp("product_createdAt"));

            Purchase purchase = new Purchase();
            purchase.setId(rs.getLong("id"));
            purchase.setCompleted(rs.getBoolean("completed"));
            purchase.setCreatedAt(rs.getTimestamp("created_at"));
            purchase.setUpdatedAt(rs.getTimestamp("updated_at"));
            purchase.setUser(user);
            purchase.setProduct(product);

            return purchase;
        }
    }
}