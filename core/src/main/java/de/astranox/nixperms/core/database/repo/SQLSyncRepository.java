package de.astranox.nixperms.core.database.repo;

import de.astranox.nixperms.core.database.SQLDatabase;
import de.astranox.nixperms.core.sync.SyncMessage;
import java.sql.*;
import java.util.*;

public final class SQLSyncRepository {

    private final SQLDatabase db;

    public SQLSyncRepository(SQLDatabase db) { this.db = db; }

    public void publish(SyncMessage message) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO nixperms_sync (server_id,type,payload,created_at) VALUES(?,?,?,?)")) {
            ps.setString(1, message.serverId()); ps.setString(2, message.type()); ps.setString(3, message.payload()); ps.setLong(4, System.currentTimeMillis()); ps.executeUpdate();
        } catch (SQLException e) { db.log().error("Failed to publish sync: {}", e.getMessage()); }
    }

    public List<SyncMessage> pollSince(long lastId, long maxAgeMs) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id,server_id,type,payload FROM nixperms_sync WHERE id>? AND created_at>? ORDER BY id ASC")) {
            ps.setLong(1, lastId); ps.setLong(2, System.currentTimeMillis() - maxAgeMs);
            ResultSet rs = ps.executeQuery(); List<SyncMessage> messages = new ArrayList<>();
            while (rs.next()) messages.add(new SyncMessage(rs.getLong("id"), rs.getString("server_id"), rs.getString("type"), rs.getString("payload")));
            return messages;
        } catch (SQLException e) { db.log().error("Failed to poll sync: {}", e.getMessage()); return List.of(); }
    }

    public void cleanup(long maxAgeMs) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM nixperms_sync WHERE created_at<?")) {
            ps.setLong(1, System.currentTimeMillis() - maxAgeMs); ps.executeUpdate();
        } catch (SQLException e) { db.log().error("Failed to cleanup sync: {}", e.getMessage()); }
    }

    public long latestId() {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(id),0) FROM nixperms_sync"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { db.log().error("Failed to fetch latest sync id: {}", e.getMessage()); }
        return 0L;
    }
}
