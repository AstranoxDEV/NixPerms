package de.astranox.nixperms.core.database;

public enum SqlDialect {
    MYSQL, MARIADB, POSTGRESQL, SQLITE;

    public static SqlDialect from(String backend) {
        return switch (backend.toLowerCase()) {
            case "mysql" -> MYSQL;
            case "mariadb" -> MARIADB;
            case "postgresql", "postgres" -> POSTGRESQL;
            case "sqlite" -> SQLITE;
            default -> throw new IllegalArgumentException("Unknown backend: " + backend);
        };
    }

    public String boolType() { return this == POSTGRESQL ? "BOOLEAN" : this == SQLITE ? "INTEGER" : "TINYINT(1)"; }

    public String upsertGroup() {
        return switch (this) {
            case MYSQL, MARIADB -> "INSERT INTO nixperms_groups (name,role,weight,parent_name) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE role=VALUES(role),weight=VALUES(weight),parent_name=VALUES(parent_name)";
            case POSTGRESQL -> "INSERT INTO nixperms_groups (name,role,weight,parent_name) VALUES(?,?,?,?) ON CONFLICT(name) DO UPDATE SET role=EXCLUDED.role,weight=EXCLUDED.weight,parent_name=EXCLUDED.parent_name";
            case SQLITE -> "INSERT OR REPLACE INTO nixperms_groups (name,role,weight,parent_name) VALUES(?,?,?,?)";
        };
    }

    public String upsertUser() {
        return switch (this) {
            case MYSQL, MARIADB -> "INSERT INTO nixperms_users (uuid,primary_group,secondary_group) VALUES(?,?,?) ON DUPLICATE KEY UPDATE primary_group=VALUES(primary_group),secondary_group=VALUES(secondary_group)";
            case POSTGRESQL -> "INSERT INTO nixperms_users (uuid,primary_group,secondary_group) VALUES(?,?,?) ON CONFLICT(uuid) DO UPDATE SET primary_group=EXCLUDED.primary_group,secondary_group=EXCLUDED.secondary_group";
            case SQLITE -> "INSERT OR REPLACE INTO nixperms_users (uuid,primary_group,secondary_group) VALUES(?,?,?)";
        };
    }
}
