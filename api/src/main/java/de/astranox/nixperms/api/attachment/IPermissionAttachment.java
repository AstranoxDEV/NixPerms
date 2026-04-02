package de.astranox.nixperms.api.attachment;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPermissionAttachment {
    UUID uniqueId();
    UUID subjectId();
    String ownerKey();
    Map<String, Boolean> permissions();
    CompletableFuture<Void> setPermission(String node, boolean value);
    CompletableFuture<Void> unsetPermission(String node);
    void invalidate();
}
