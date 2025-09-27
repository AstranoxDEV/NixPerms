package de.syntaxjason.nixperms.api.event.user;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.user.IUser;

import java.util.Optional;

public interface IUserMetaChangeEvent extends INixEvent, ICancellable {

    IUser user();
    String key();
    Optional<String> oldValue();
    String newValue();
    void setNewValue(String newValue);

}
