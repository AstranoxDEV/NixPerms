package de.syntaxjason.nixperms.database;

import de.syntaxjason.annotations.*;
import de.syntaxjason.core.DatabaseEntity;
import de.syntaxjason.types.CacheStrategy;
import de.syntaxjason.types.DatabaseType;
import de.syntaxjason.types.FieldType;

@SoftDelete(column = "deleted_at")
@DatabaseTable(
        name = "users",
        supportedTypes = { DatabaseType.MYSQL, DatabaseType.SQLITE, DatabaseType.POSTGRESQL, DatabaseType.MONGODB },
        indexes = { "idx_groupname" },
        cacheStrategy = CacheStrategy.AGGRESSIVE
)
@Index(name = "idx_groupname_groupuuid", fields = { "groupname" })
public class GroupData implements DatabaseEntity {

    @PrimaryKey(strategy = PrimaryKey.GenerationType.UUID)
    @DatabaseField(column = "id", nullable = false, type = FieldType.STRING)
    private String uniqueId;

    @DatabaseField(column = "groupdata", nullable = false, type = FieldType.JSON)
    private GroupData groupData;

}
