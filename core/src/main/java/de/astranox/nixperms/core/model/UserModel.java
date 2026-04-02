package de.astranox.nixperms.core.model;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.UUID;

public record UserModel(UUID uniqueId, String primaryGroupName, @Nullable String secondaryGroupName, Map<String, Boolean> permissions) {}
