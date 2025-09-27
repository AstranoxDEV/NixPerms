package de.syntaxjason.nixperms.api.event.user;

import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.user.IUser;

public interface IUserDataRecalculateEvent extends INixEvent {

    IUser user();

}
