package de.astranox.nixperms.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.astranox.nixperms.core.config.DatabaseConfig;

public final class HikariPoolFactory {

    private HikariPoolFactory() {}

    public static HikariDataSource create(DatabaseConfig config, SqlDialect dialect) {
        HikariConfig hikari = new HikariConfig();
        hikari.setMaximumPoolSize(config.poolSize);
        hikari.setPoolName("nixperms-pool");

        switch (dialect) {
            case POSTGRESQL -> hikari.setDriverClassName("org.postgresql.Driver");
            case MYSQL, MARIADB -> hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
            case SQLITE -> {
                hikari.setDriverClassName("org.sqlite.JDBC");
                hikari.setJdbcUrl("jdbc:sqlite:" + config.database + ".db");
                return new HikariDataSource(hikari);
            }
        }

        String driverPrefix = dialect == SqlDialect.POSTGRESQL ? "postgresql" : "mysql";
        hikari.setJdbcUrl("jdbc:" + driverPrefix + "://" + config.host + ":" + config.port + "/" + config.database + "?useSSL=false&autoReconnect=true");
        hikari.setUsername(config.username);
        hikari.setPassword(config.password);

        return new HikariDataSource(hikari);
    }
}
