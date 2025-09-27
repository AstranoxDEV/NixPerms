package de.syntaxjason.nixperms.config;

import de.syntaxjason.nixperms.config.lib.NixConfiguration;
import de.syntaxjason.nixperms.config.lib.Value;
import de.syntaxjason.nixperms.file.NixObject;

@NixConfiguration(file = "sync.nix")
public class SyncConfig {
    @Value(key = "sync", def = """
            
            """)
    private NixObject sync;
}
