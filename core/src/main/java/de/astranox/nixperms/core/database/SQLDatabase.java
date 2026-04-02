package de.astranox.nixperms.core.database;

import com.zaxxer.hikari.HikariDataSource;
import de.astranox.nixperms.core.database.repo.*;
import de.astranox.nixperms.core.model.*;
import de.astranox.nixperms.core.sync.SyncMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class SQLDatabase {

    private final HikariDataSource pool;
    private final Logger logger = LoggerFactory.getLogger(SQLDatabase.class);
    private final SQLGroupRepository groupRepo;
    private final SQLUserRepository userRepo;
    private final SQLSyncRepository syncRepo;

    public SQLDatabase(HikariDataSource pool, SqlDialect dialect) {
        this.pool = pool;
        this.groupRepo = new SQLGroupRepository(this, dialect);
        this.userRepo = new SQLUserRepository(this, dialect);
        this.syncRepo = new SQLSyncRepository(this);
        try (Connection conn = getConnection()) { SchemaInitializer.initialize(conn, dialect); }
        catch (SQLException e) { throw new RuntimeException("Schema initialization failed", e); }
    }

    public Connection getConnection() throws SQLException { return pool.getConnection(); }
    public Logger log() { return logger; }
    public void disconnect() { pool.close(); }

    public GroupModel getGroup(String name) { return groupRepo.get(name); }
    public Collection<GroupModel> getAllGroups() { return groupRepo.getAll(); }
    public void saveGroup(GroupModel model) { groupRepo.save(model); }
    public void deleteGroup(String name) { groupRepo.delete(name); }
    public UserModel getUser(UUID uuid) { return userRepo.get(uuid); }
    public void saveUser(UserModel model) { userRepo.save(model); }
    public void publishSync(SyncMessage message) { syncRepo.publish(message); }
    public List<SyncMessage> pollSync(long lastId, long maxAgeMs) { return syncRepo.pollSince(lastId, maxAgeMs); }
    public void cleanupSync(long maxAgeMs) { syncRepo.cleanup(maxAgeMs); }
    public long latestSyncId() { return syncRepo.latestId(); }
}
