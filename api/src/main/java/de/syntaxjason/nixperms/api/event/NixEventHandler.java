package de.syntaxjason.nixperms.api.event;

public @interface NixEventHandler {
    NixPriority priority() default NixPriority.NORMAL;
    boolean ignoreCancelled() default true;
}
