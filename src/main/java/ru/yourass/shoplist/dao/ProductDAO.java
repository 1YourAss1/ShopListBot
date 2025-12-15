package ru.yourass.shoplist.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yourass.shoplist.model.Product;

import java.util.Objects;

@Component
public class ProductDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Product getByName(String name) {
        String normalizedName = normalizeProductName(name);
        String sql = "SELECT * FROM products WHERE name = :name";
        try {
            return jdbcTemplate.queryForObject(sql,
                    new MapSqlParameterSource("name", normalizedName),
                    new BeanPropertyRowMapper<>(Product.class));
        } catch (EmptyResultDataAccessException e) {
            Product product = new Product();
            product.setName(normalizedName);
            product.setId(this.save(product));
            return product;
        }
    }

    public Long save(Product product) {
        String sql = """
            INSERT INTO products (name)
            VALUES (:name)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource("name", product.getName());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }


    private String normalizeProductName(String productName) {
        return productName.trim().toLowerCase();
    }
}
