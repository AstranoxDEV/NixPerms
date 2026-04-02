package de.astranox.nixperms.core.storage;

import de.astranox.nixperms.core.database.SQLDatabase;
import de.astranox.nixperms.core.model.GroupModel;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class GroupStorageAdapter implements IGroupStorage {

    private final SQLDatabase database;
    private final Executor executor;

    public GroupStorageAdapter(SQLDatabase database, Executor executor) {
        this.database = database;
        this.executor = executor;
    }

    @Override public CompletableFuture<Collection<GroupModel>> loadAll() { return CompletableFuture.supplyAsync(database::getAllGroups, executor); }
    @Override public CompletableFuture<@Nullable GroupModel> load(String name) { return CompletableFuture.supplyAsync(() -> database.getGroup(name), executor); }
    @Override public CompletableFuture<Void> save(GroupModel model) { return CompletableFuture.runAsync(() -> database.saveGroup(model), executor); }
    @Override public CompletableFuture<Void> delete(String name) { return CompletableFuture.runAsync(() -> database.deleteGroup(name), executor); }
}
