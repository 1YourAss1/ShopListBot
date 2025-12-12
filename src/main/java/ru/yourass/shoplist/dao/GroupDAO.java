package ru.yourass.shoplist.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.Group;
import ru.yourass.shoplist.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Component
public class GroupDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SQL_SELECT_GROUP = """
            SELECT
                groups.id as "id",
                groups.created_at as "created_at",
                users.id as "user_id",
                users.username as "user_name",
                users.first_name as "user_first_name",
                users.last_name as "user_last_name",
                users.created_at as "user_created_at"
                FROM groups
                INNER JOIN users ON users.id = groups.owner_id
            """;

    public GroupDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Optional<Group> findById(Long id) {
        String sql = SQL_SELECT_GROUP + """
                WHERE groups.id = :groupId
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    new MapSqlParameterSource("groupId", id),
                    new GroupMapper())
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Long save(Group group) {
        String sql = """
            INSERT INTO groups (owner_id)
            VALUES (:ownerId)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource("ownerId", group.getOwner().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<Group> getByUser(User user) {
        String sql = SQL_SELECT_GROUP + """
                WHERE groups.owner_id = :userId
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    new MapSqlParameterSource("userId", user.getId()),
                    new GroupMapper())
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static class GroupMapper implements RowMapper<Group> {
        @Override
        public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setFirstName(rs.getString("user_first_name"));
            user.setLastName(rs.getString("user_last_name"));
            user.setCreatedAt(rs.getTimestamp("user_created_at"));

            Group group = new Group();
            group.setId(rs.getLong("id"));
            group.setOwner(user);
            return group;
        }
    }
}
