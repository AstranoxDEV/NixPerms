package de.astranox.nixperms.core.user;

import de.astranox.nixperms.api.permission.IPermissionData;
import de.astranox.nixperms.api.user.*;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public record NixUserSnapshot(UUID uniqueId, String primaryGroupName, @Nullable String secondaryExplicitName, @Nullable String secondaryEffectiveName, IPermissionData permissions, NixMetaData meta) implements IUserSnapshot {
    @Override public NixMetaData meta() { return meta; }
}
