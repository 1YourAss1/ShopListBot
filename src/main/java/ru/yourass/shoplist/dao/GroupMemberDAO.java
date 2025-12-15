package ru.yourass.shoplist.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yourass.shoplist.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class GroupMemberDAO {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String GROUP_ID = "groupId";
    private static final String USER_ID = "userId";
    private static final String STATUS = "status";

    private static final String SQL_SELECT_GROUP_MEMBER = """
            SELECT
                group_members.status as "status",
                group_members.created_at as "created_at",
                groups.id as "group_id",
                groups.created_at as "group_created_at",
                owner.id as "owner_id",
                owner.username as "owner_name",
                owner.first_name as "owner_first_name",
                owner.last_name as "owner_last_name",
                owner.created_at as "owner_created_at",
                users.id as "user_id",
                users.username as "user_name",
                users.first_name as "user_first_name",
                users.last_name as "user_last_name",
                users.created_at as "user_created_at"
            FROM group_members
            INNER JOIN groups ON groups.id = group_members.group_id
            INNER JOIN users owner ON groups.owner_id = owner.id
            INNER JOIN users ON users.id = group_members.user_id
            """;

    public GroupMemberDAO(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(GroupMember groupMember) {
        String sql = """
            INSERT INTO group_members (group_id, user_id, status)\s
            VALUES (:groupId, :userId, :status)
        """;
        jdbcTemplate.update(sql,
                new MapSqlParameterSource()
                        .addValue(GROUP_ID, groupMember.getGroup().getId())
                        .addValue(USER_ID, groupMember.getUser().getId())
                        .addValue(STATUS, groupMember.getStatus()));
    }

    public void updateStatus(GroupMember groupMember) {
        String sql = """
                    UPDATE group_members
                    SET status = :status
                    WHERE group_id = :groupId AND user_id = :userId
            """;

        int rows = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue(STATUS, groupMember.getStatus())
                .addValue(GROUP_ID, groupMember.getGroup().getId())
                .addValue(USER_ID, groupMember.getUser().getId()));
        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, rows);
        }
    }

    public Optional<GroupMember> getByGroupAndUser(Group group, User user) {
        String sql = SQL_SELECT_GROUP_MEMBER + """
            WHERE group_id = :groupId AND user_id = :userId
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    new MapSqlParameterSource()
                            .addValue(GROUP_ID, group.getId())
                            .addValue(USER_ID, user.getId()),
                    new GroupMemberRowMapper())
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<GroupMember> getByUser(User user) {
        String sql = SQL_SELECT_GROUP_MEMBER + """
            WHERE user_id = :userId
        """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(USER_ID, user.getId()),
                new GroupMemberRowMapper());
    }

    public Optional<GroupMember> getActiveByUserId(Long userId) {
        String sql = SQL_SELECT_GROUP_MEMBER + """
            WHERE user_id = :userId AND status != 'inactive'
            LIMIT 1
        """;
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                        sql,
                        new MapSqlParameterSource(USER_ID, userId),
                        new GroupMemberRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    public List<GroupMember> getActiveByGroup(Group group) {
        String sql = SQL_SELECT_GROUP_MEMBER + """
            WHERE group_id = :groupId AND status != 'inactive'
        """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(GROUP_ID, group.getId()),
                new GroupMemberRowMapper());
    }

    private static class GroupMemberRowMapper implements RowMapper<GroupMember> {
        @Override
        public GroupMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setFirstName(rs.getString("user_first_name"));
            user.setLastName(rs.getString("user_last_name"));
            user.setCreatedAt(rs.getTimestamp("user_created_at"));

            User owner = new User();
            owner.setId(rs.getLong("owner_id"));
            owner.setUserName(rs.getString("owner_name"));
            owner.setFirstName(rs.getString("owner_first_name"));
            owner.setLastName(rs.getString("owner_last_name"));
            owner.setCreatedAt(rs.getTimestamp("owner_created_at"));

            Group group = new Group();
            group.setId(rs.getLong("group_id"));
            group.setOwner(owner);

            GroupMember groupMember = new GroupMember();
            groupMember.setGroup(group);
            groupMember.setUser(user);
            groupMember.setStatus(rs.getString(STATUS));
            groupMember.setCreatedAt(rs.getTimestamp("created_at"));

            return groupMember;
        }
    }
}
