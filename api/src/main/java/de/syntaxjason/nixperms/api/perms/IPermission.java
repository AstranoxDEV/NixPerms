package de.syntaxjason.nixperms.api.perms;

import de.syntaxjason.nixperms.api.util.IMetaHolder;

import java.util.OptionalLong;

public interface IPermission extends IMetaHolder {
    String name();
    boolean value();
    OptionalLong expiresAt();
}
