package de.astranox.nixperms.api.permission;

public enum PermissionDecision {
    TRUE,
    FALSE,
    UNSET;

    public boolean toBoolean() { return this == TRUE; }
}
