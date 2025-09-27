package de.syntaxjason.nixperms.database.codec;

import de.syntaxjason.json.JsonCodec;
import de.syntaxjason.nixperms.database.GroupData;

public class GroupDataCodec implements JsonCodec<GroupData> {
    @Override
    public Class<GroupData> type() {
        return null;
    }

    @Override
    public String serialize(Object object) {
        return "";
    }

    @Override
    public GroupData deserialize(String string) {
        return null;
    }
}
