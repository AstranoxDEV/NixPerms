package de.astranox.nixperms.core.storage;

import de.astranox.nixperms.core.model.GroupModel;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface IGroupStorage {
    CompletableFuture<Collection<GroupModel>> loadAll();
    CompletableFuture<@Nullable GroupModel> load(String name);
    CompletableFuture<Void> save(GroupModel model);
    CompletableFuture<Void> delete(String name);
}
