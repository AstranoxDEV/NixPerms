package de.astranox.nixperms.core.storage;

import de.astranox.nixperms.core.model.UserModel;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserStorage {
    CompletableFuture<@Nullable UserModel> load(UUID uniqueId);
    CompletableFuture<Void> save(UserModel model);
}
