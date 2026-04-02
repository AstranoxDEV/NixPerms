package de.astranox.nixperms.core.util;

import de.astranox.nixperms.core.config.WebConfig;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ServerAddressResolver {

    private ServerAddressResolver() {}

    public static String resolve(WebConfig config) {
        if (config.publicAddress != null && !config.publicAddress.isBlank()) return config.publicAddress;
        try { return InetAddress.getLocalHost().getHostAddress(); }
        catch (UnknownHostException e) { return "localhost"; }
    }
}
