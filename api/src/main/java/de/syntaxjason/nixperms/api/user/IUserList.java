package de.syntaxjason.nixperms.api.user;

import java.util.Set;
import java.util.UUID;

public interface IUserList {
    Set<UUID> userIds();
    Set<IUser> users();
}