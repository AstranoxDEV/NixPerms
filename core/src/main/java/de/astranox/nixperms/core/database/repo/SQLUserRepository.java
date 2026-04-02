package de.astranox.nixperms.core.database.repo;

import de.astranox.nixperms.core.database.SQLDatabase;
import de.astranox.nixperms.core.database.SqlDialect;
import de.astranox.nixperms.core.model.UserModel;
import java.sql.*;
import java.util.*;

public final class SQLUserRepository {

    private final SQLDatabase db;
    private final SqlDialect dialect;

    public SQLUserRepository(SQLDatabase db, SqlDialect dialect) {
        this.db = db;
        this.dialect = dialect;
    }

    public UserModel get(UUID uuid) {
        try (Connection conn = db.getConnection()) {
            String primaryGroup; String secondaryGroup;
            try (PreparedStatement ps = conn.prepareStatement("SELECT primary_group,secondary_group FROM nixperms_users WHERE uuid=?")) {
                ps.setString(1, uuid.toString()); ResultSet rs = ps.executeQuery(); if (!rs.next()) return null;
                primaryGroup = rs.getString("primary_group"); secondaryGroup = rs.getString("secondary_group");
            }
            Map<String, Boolean> permissions = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT node,value FROM nixperms_user_permissions WHERE uuid=?")) {
                ps.setString(1, uuid.toString()); ResultSet rs = ps.executeQuery(); while (rs.next()) permissions.put(rs.getString("node"), rs.getBoolean("value"));
            }
            return new UserModel(uuid, primaryGroup, secondaryGroup, Collections.unmodifiableMap(permissions));
        } catch (SQLException e) { db.log().error("Failed to get user '{}': {}", uuid, e.getMessage()); return null; }
    }

    public void save(UserModel model) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(dialect.upsertUser())) { ps.setString(1, model.uniqueId().toString()); ps.setString(2, model.primaryGroupName()); ps.setString(3, model.secondaryGroupName()); ps.executeUpdate(); }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nixperms_user_permissions WHERE uuid=?")) { ps.setString(1, model.uniqueId().toString()); ps.executeUpdate(); }
                if (!model.permissions().isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO nixperms_user_permissions (uuid,node,value) VALUES(?,?,?)")) {
                        for (Map.Entry<String, Boolean> e : model.permissions().entrySet()) { ps.setString(1, model.uniqueId().toString()); ps.setString(2, e.getKey()); ps.setBoolean(3, e.getValue()); ps.addBatch(); }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { db.log().error("Failed to save user '{}': {}", model.uniqueId(), e.getMessage()); }
    }
}
