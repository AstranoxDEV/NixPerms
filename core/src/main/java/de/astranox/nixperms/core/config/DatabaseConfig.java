package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;
import de.astranox.nixperms.api.annotation.config.Reload;

@ConfigSection("database")
public final class DatabaseConfig {
    @Key(value = "backend", comment = "mysql | mariadb | postgresql | sqlite") public String backend = "mariadb";
    @Key("host") public String host = "localhost";
    @Key("port") public int port = 3306;
    @Key("database") public String database = "nixperms";
    @Key("username") public String username = "root";
    @Key("password") public String password = "";
    @Key(value = "pool-size", comment = "HikariCP pool size") public int poolSize = 10;
}
