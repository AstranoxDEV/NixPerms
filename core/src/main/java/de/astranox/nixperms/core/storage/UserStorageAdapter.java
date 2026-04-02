package de.astranox.nixperms.core.storage;

import de.astranox.nixperms.core.database.SQLDatabase;
import de.astranox.nixperms.core.model.UserModel;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class UserStorageAdapter implements IUserStorage {

    private final SQLDatabase database;
    private final Executor executor;

    public UserStorageAdapter(SQLDatabase database, Executor executor) {
        this.database = database;
        this.executor = executor;
    }

    @Override public CompletableFuture<@Nullable UserModel> load(UUID uuid) { return CompletableFuture.supplyAsync(() -> database.getUser(uuid), executor); }
    @Override public CompletableFuture<Void> save(UserModel model) { return CompletableFuture.runAsync(() -> database.saveUser(model), executor); }
}
