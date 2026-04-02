package de.astranox.nixperms.core.web;

import java.util.UUID;

public interface WebEditorBridge {
    boolean isRunning();
    int port();
    String serverAddress();
    String createSessionToken(UUID playerId);
}
