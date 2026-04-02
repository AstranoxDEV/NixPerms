package de.astranox.nixperms.core.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaInitializer {

    private SchemaInitializer() {}

    public static void initialize(Connection conn, SqlDialect dialect) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_groups (name VARCHAR(64) NOT NULL PRIMARY KEY,role VARCHAR(16) NOT NULL,weight INT NOT NULL DEFAULT 0,parent_name VARCHAR(64) NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_group_permissions (group_name VARCHAR(64) NOT NULL,node VARCHAR(256) NOT NULL,value " + dialect.boolType() + " NOT NULL,PRIMARY KEY(group_name,node))");
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_group_meta (group_name VARCHAR(64) NOT NULL,type VARCHAR(8) NOT NULL,priority INT NOT NULL,value VARCHAR(256) NOT NULL,PRIMARY KEY(group_name,type,priority,value))");
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_group_options (group_name VARCHAR(64) NOT NULL,key_name VARCHAR(64) NOT NULL,value VARCHAR(256) NOT NULL,PRIMARY KEY(group_name,key_name))");
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_users (uuid CHAR(36) NOT NULL PRIMARY KEY,primary_group VARCHAR(64) NOT NULL DEFAULT 'default',secondary_group VARCHAR(64) NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS nixperms_user_permissions (uuid CHAR(36) NOT NULL,node VARCHAR(256) NOT NULL,value " + dialect.boolType() + " NOT NULL,PRIMARY KEY(uuid,node))");
            st.execute(syncTable(dialect));
        }
    }

    private static String syncTable(SqlDialect dialect) {
        return switch (dialect) {
            case SQLITE -> "CREATE TABLE IF NOT EXISTS nixperms_sync (id INTEGER PRIMARY KEY AUTOINCREMENT,server_id VARCHAR(64) NOT NULL,type VARCHAR(32) NOT NULL,payload VARCHAR(256) NULL,created_at BIGINT NOT NULL)";
            case POSTGRESQL -> "CREATE TABLE IF NOT EXISTS nixperms_sync (id BIGSERIAL PRIMARY KEY,server_id VARCHAR(64) NOT NULL,type VARCHAR(32) NOT NULL,payload VARCHAR(256) NULL,created_at BIGINT NOT NULL)";
            default -> "CREATE TABLE IF NOT EXISTS nixperms_sync (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,server_id VARCHAR(64) NOT NULL,type VARCHAR(32) NOT NULL,payload VARCHAR(256) NULL,created_at BIGINT NOT NULL)";
        };
    }
}
