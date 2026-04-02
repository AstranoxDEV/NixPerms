package de.astranox.nixperms.core.sync;

import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.network.NetworkSyncEvent;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public final class SyncMessenger {

    private static final long MAX_AGE_MS = 60_000L;

    private final SQLSyncStorage storage;
    private final IEventBus eventBus;
    private final String serverId;
    private final long pollIntervalMs;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "nixperms-sync"); t.setDaemon(true); return t; });
    private volatile long lastProcessedId = 0L;
    private ScheduledFuture<?> pollTask;

    public SyncMessenger(SQLSyncStorage storage, IEventBus eventBus, String serverId, long pollIntervalMs) {
        this.storage = storage;
        this.eventBus = eventBus;
        this.serverId = serverId;
        this.pollIntervalMs = pollIntervalMs;
    }

    public void start() { lastProcessedId = storage.latestId(); pollTask = scheduler.scheduleAtFixedRate(this::poll, pollIntervalMs, pollIntervalMs, TimeUnit.MILLISECONDS); }
    public void stop() { if (pollTask != null) pollTask.cancel(false); scheduler.shutdownNow(); storage.cleanup(MAX_AGE_MS); }
    public void publishUserUpdate(UUID userId) { storage.publish(new SyncMessage(0, serverId, "USER_UPDATE", userId.toString())); }
    public void publishGroupUpdate(String groupName) { storage.publish(new SyncMessage(0, serverId, "GROUP_UPDATE", groupName)); }
    public void publishGlobalInvalidation() { storage.publish(new SyncMessage(0, serverId, "GLOBAL_INVALIDATION", null)); }

    private void poll() {
        List<SyncMessage> messages = storage.pollSince(lastProcessedId, MAX_AGE_MS);
        for (SyncMessage message : messages) {
            lastProcessedId = message.id();
            if (serverId.equals(message.serverId())) continue;
            dispatchEvent(message);
        }
    }

    private void dispatchEvent(SyncMessage message) {
        NetworkSyncEvent event = switch (message.type()) {
            case "USER_UPDATE" -> new NetworkSyncEvent(NetworkSyncEvent.SyncType.USER_UPDATE, UUID.fromString(message.payload()), null, message.serverId());
            case "GROUP_UPDATE" -> new NetworkSyncEvent(NetworkSyncEvent.SyncType.GROUP_UPDATE, null, message.payload(), message.serverId());
            default -> new NetworkSyncEvent(NetworkSyncEvent.SyncType.GLOBAL_INVALIDATION, null, null, message.serverId());
        };
        eventBus.post(event);
    }
}
