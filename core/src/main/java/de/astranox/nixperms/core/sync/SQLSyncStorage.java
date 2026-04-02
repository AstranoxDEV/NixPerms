package de.astranox.nixperms.core.sync;

import de.astranox.nixperms.core.database.SQLDatabase;
import java.util.List;

public final class SQLSyncStorage {

    private final SQLDatabase db;

    public SQLSyncStorage(SQLDatabase db) { this.db = db; }

    public void publish(SyncMessage message) { db.publishSync(message); }
    public List<SyncMessage> pollSince(long lastId, long maxAgeMs) { return db.pollSync(lastId, maxAgeMs); }
    public void cleanup(long maxAgeMs) { db.cleanupSync(maxAgeMs); }
    public long latestId() { return db.latestSyncId(); }
}
