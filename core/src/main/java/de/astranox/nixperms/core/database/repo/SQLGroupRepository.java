package de.astranox.nixperms.core.database.repo;

import de.astranox.nixperms.api.group.GroupRole;
import de.astranox.nixperms.core.database.SQLDatabase;
import de.astranox.nixperms.core.database.SqlDialect;
import de.astranox.nixperms.core.model.*;
import java.sql.*;
import java.util.*;

public final class SQLGroupRepository {

    private final SQLDatabase db;
    private final SqlDialect dialect;

    public SQLGroupRepository(SQLDatabase db, SqlDialect dialect) {
        this.db = db;
        this.dialect = dialect;
    }

    public GroupModel get(String name) {
        try (Connection conn = db.getConnection()) {
            GroupModel.Builder builder = loadBase(conn, name);
            if (builder == null) return null;
            loadChildren(conn, name, builder);
            return builder.build();
        } catch (SQLException e) { db.log().error("Failed to get group '{}': {}", name, e.getMessage()); return null; }
    }

    public Collection<GroupModel> getAll() {
        try (Connection conn = db.getConnection()) {
            Map<String, GroupModel.Builder> builders = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT name,role,weight,parent_name FROM nixperms_groups"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) builders.put(rs.getString("name"), new GroupModel.Builder(rs.getString("name"), GroupRole.valueOf(rs.getString("role")), rs.getInt("weight"), rs.getString("parent_name")));
            }
            if (builders.isEmpty()) return List.of();
            try (PreparedStatement ps = conn.prepareStatement("SELECT group_name,node,value FROM nixperms_group_permissions"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { GroupModel.Builder b = builders.get(rs.getString("group_name")); if (b != null) b.permission(rs.getString("node"), rs.getBoolean("value")); }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT group_name,type,priority,value FROM nixperms_group_meta"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { GroupModel.Builder b = builders.get(rs.getString("group_name")); if (b == null) continue; MetaEntryModel entry = new MetaEntryModel(rs.getInt("priority"), rs.getString("value")); if ("prefix".equalsIgnoreCase(rs.getString("type"))) b.prefix(entry); else b.suffix(entry); }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT group_name,key_name,value FROM nixperms_group_options"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { GroupModel.Builder b = builders.get(rs.getString("group_name")); if (b != null) b.option(rs.getString("key_name"), rs.getString("value")); }
            }
            return builders.values().stream().map(GroupModel.Builder::build).toList();
        } catch (SQLException e) { db.log().error("Failed to get all groups: {}", e.getMessage()); return List.of(); }
    }

    public void save(GroupModel model) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(dialect.upsertGroup())) { ps.setString(1, model.name()); ps.setString(2, model.role().name()); ps.setInt(3, model.weight()); ps.setString(4, model.parentName()); ps.executeUpdate(); }
                deleteChildren(conn, model.name());
                insertChildren(conn, model);
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { db.log().error("Failed to save group '{}': {}", model.name(), e.getMessage()); }
    }

    public void delete(String name) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                deleteChildren(conn, name);
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nixperms_groups WHERE name=?")) { ps.setString(1, name); ps.executeUpdate(); }
                conn.commit();
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { db.log().error("Failed to delete group '{}': {}", name, e.getMessage()); }
    }

    private GroupModel.Builder loadBase(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT name,role,weight,parent_name FROM nixperms_groups WHERE name=?")) {
            ps.setString(1, name); ResultSet rs = ps.executeQuery(); if (!rs.next()) return null;
            return new GroupModel.Builder(rs.getString("name"), GroupRole.valueOf(rs.getString("role")), rs.getInt("weight"), rs.getString("parent_name"));
        }
    }

    private void loadChildren(Connection conn, String name, GroupModel.Builder builder) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT node,value FROM nixperms_group_permissions WHERE group_name=?")) { ps.setString(1, name); ResultSet rs = ps.executeQuery(); while (rs.next()) builder.permission(rs.getString("node"), rs.getBoolean("value")); }
        try (PreparedStatement ps = conn.prepareStatement("SELECT type,priority,value FROM nixperms_group_meta WHERE group_name=?")) { ps.setString(1, name); ResultSet rs = ps.executeQuery(); while (rs.next()) { MetaEntryModel entry = new MetaEntryModel(rs.getInt("priority"), rs.getString("value")); if ("prefix".equalsIgnoreCase(rs.getString("type"))) builder.prefix(entry); else builder.suffix(entry); } }
        try (PreparedStatement ps = conn.prepareStatement("SELECT key_name,value FROM nixperms_group_options WHERE group_name=?")) { ps.setString(1, name); ResultSet rs = ps.executeQuery(); while (rs.next()) builder.option(rs.getString("key_name"), rs.getString("value")); }
    }

    private void deleteChildren(Connection conn, String name) throws SQLException {
        for (String sql : List.of("DELETE FROM nixperms_group_permissions WHERE group_name=?", "DELETE FROM nixperms_group_meta WHERE group_name=?", "DELETE FROM nixperms_group_options WHERE group_name=?")) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setString(1, name); ps.executeUpdate(); }
        }
    }

    private void insertChildren(Connection conn, GroupModel model) throws SQLException {
        if (!model.permissions().isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO nixperms_group_permissions (group_name,node,value) VALUES(?,?,?)")) {
                for (Map.Entry<String, Boolean> e : model.permissions().entrySet()) { ps.setString(1, model.name()); ps.setString(2, e.getKey()); ps.setBoolean(3, e.getValue()); ps.addBatch(); }
                ps.executeBatch();
            }
        }
        if (!model.prefixes().isEmpty() || !model.suffixes().isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO nixperms_group_meta (group_name,type,priority,value) VALUES(?,?,?,?)")) {
                for (MetaEntryModel m : model.prefixes()) { ps.setString(1, model.name()); ps.setString(2, "prefix"); ps.setInt(3, m.priority()); ps.setString(4, m.value()); ps.addBatch(); }
                for (MetaEntryModel m : model.suffixes()) { ps.setString(1, model.name()); ps.setString(2, "suffix"); ps.setInt(3, m.priority()); ps.setString(4, m.value()); ps.addBatch(); }
                ps.executeBatch();
            }
        }
        if (!model.options().isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO nixperms_group_options (group_name,key_name,value) VALUES(?,?,?)")) {
                for (Map.Entry<String, String> e : model.options().entrySet()) { ps.setString(1, model.name()); ps.setString(2, e.getKey()); ps.setString(3, e.getValue()); ps.addBatch(); }
                ps.executeBatch();
            }
        }
    }
}
