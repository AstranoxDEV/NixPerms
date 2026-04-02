package de.astranox.nixperms.api.attachment;

import java.util.Collection;
import java.util.UUID;

public interface IAttachmentService {
    IPermissionAttachment create(UUID subjectId, String ownerKey);
    Collection<IPermissionAttachment> getAttachments(UUID subjectId);
    void invalidateAll(UUID subjectId);
    void invalidateByOwner(String ownerKey);
}
