package de.astranox.nixperms.core.attachment;

import de.astranox.nixperms.api.attachment.IPermissionAttachment;
import de.astranox.nixperms.api.event.IEventBus;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NixPermissionAttachment implements IPermissionAttachment {

    private final UUID uniqueId;
    private final UUID subjectId;
    private final String ownerKey;
    private final Object2BooleanOpenHashMap<String> permissions = new Object2BooleanOpenHashMap<>();
    private final Runnable onInvalidate;

    public NixPermissionAttachment(UUID subjectId, String ownerKey, Runnable onInvalidate) {
        this.uniqueId = UUID.randomUUID();
        this.subjectId = subjectId;
        this.ownerKey = ownerKey;
        this.onInvalidate = onInvalidate;
    }

    @Override public UUID uniqueId() { return uniqueId; }
    @Override public UUID subjectId() { return subjectId; }
    @Override public String ownerKey() { return ownerKey; }
    @Override public Map<String, Boolean> permissions() { return permissions; }

    @Override
    public CompletableFuture<Void> setPermission(String node, boolean value) {
        permissions.put(node, value);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unsetPermission(String node) {
        permissions.remove(node);
        return CompletableFuture.completedFuture(null);
    }

    @Override public void invalidate() { onInvalidate.run(); }
}
