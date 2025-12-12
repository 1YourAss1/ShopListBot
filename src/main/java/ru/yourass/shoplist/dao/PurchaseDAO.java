package ru.yourass.shoplist.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PurchaseDAO {
    private static final String USER_ID = "userId";
    private static final String COMPLETED = "completed";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";

    private static final String SQL_SELECT_PURCHASE = """
                        SELECT
                            purchases.id as "id",
                            purchases.quantity as "quantity",
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
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PurchaseDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Purchase> getByProductForUser(Product product, User user) {
        String sql = SQL_SELECT_PURCHASE + """
                    WHERE purchases.user_id = :userId AND purchases.product_id = :productId
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(USER_ID, user.getId())
                            .addValue("productId", product.getId()),
                    new PurchaseProductRowMapper())
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(Purchase purchase) {
        Objects.requireNonNull(purchase);
        Objects.requireNonNull(purchase.getUser());
        Objects.requireNonNull(purchase.getProduct());

        String sql = """
            INSERT INTO purchases (user_id, product_id, completed, created_at, updated_at)\s
            VALUES (:userId, :productId, :completed, :createdAt, :updatedAt)
           """;
        jdbcTemplate.update(sql,
                new MapSqlParameterSource()
                        .addValue(USER_ID, purchase.getUser().getId())
                        .addValue("productId", purchase.getProduct().getId())
                        .addValue(COMPLETED, purchase.isCompleted())
                        .addValue(CREATED_AT, LocalDateTime.now())
                        .addValue(UPDATED_AT, LocalDateTime.now()));
    }

    public List<Purchase> getAllByUserId(Long userId) {
        String sql = SQL_SELECT_PURCHASE + """
                        WHERE purchases.user_id = :userId
                        ORDER BY purchases.created_at DESC;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(USER_ID, userId),
                new PurchaseProductRowMapper());
    }

    public List<Purchase> getIncompletedByUserId(Long userId) {
        String sql = SQL_SELECT_PURCHASE + """
                        WHERE purchases.user_id = :userId AND completed = false
                        ORDER BY purchases.updated_at DESC;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(USER_ID, userId),
                new PurchaseProductRowMapper());
    }

    public List<Purchase> getCompletedByUserId(Long userId, int count) {
        String sql = SQL_SELECT_PURCHASE + """
                        WHERE purchases.user_id = :userId AND completed = true
                        ORDER BY purchases.updated_at DESC
                        LIMIT :count;
           """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(USER_ID, userId)
                        .addValue("count", count),
                new PurchaseProductRowMapper());
    }

    public void update(Purchase purchase) {
        String sql = """
            UPDATE purchases
            SET completed = :completed, updated_at = :updatedAt, quantity = :quantity
            WHERE id = :id
        """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue(COMPLETED, purchase.isCompleted())
                .addValue(UPDATED_AT, LocalDateTime.now())
                .addValue("quantity", purchase.getQuantity())
                .addValue("id", purchase.getId()));
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
            purchase.setUser(user);
            purchase.setProduct(product);
            purchase.setQuantity(rs.getFloat("quantity"));
            purchase.setCompleted(rs.getBoolean(COMPLETED));
            purchase.setCreatedAt(rs.getTimestamp("created_at"));
            purchase.setUpdatedAt(rs.getTimestamp("updated_at"));

            return purchase;
        }
    }
}