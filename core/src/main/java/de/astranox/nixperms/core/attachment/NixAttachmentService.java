package de.astranox.nixperms.core.attachment;

import de.astranox.nixperms.api.attachment.IAttachmentService;
import de.astranox.nixperms.api.attachment.IPermissionAttachment;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NixAttachmentService implements IAttachmentService {

    private final Object2ObjectOpenHashMap<UUID, List<NixPermissionAttachment>> attachments = new Object2ObjectOpenHashMap<>();

    @Override
    public IPermissionAttachment create(UUID subjectId, String ownerKey) {
        NixPermissionAttachment attachment = new NixPermissionAttachment(subjectId, ownerKey, () -> invalidateByOwner(ownerKey));
        attachments.computeIfAbsent(subjectId, k -> new CopyOnWriteArrayList<>()).add(attachment);
        return attachment;
    }

    @Override
    public Collection<IPermissionAttachment> getAttachments(UUID subjectId) {
        List<NixPermissionAttachment> list = attachments.get(subjectId);
        return list != null ? Collections.unmodifiableList(list) : List.of();
    }

    @Override
    public void invalidateAll(UUID subjectId) { attachments.remove(subjectId); }

    @Override
    public void invalidateByOwner(String ownerKey) {
        attachments.values().forEach(list -> list.removeIf(a -> a.ownerKey().equals(ownerKey)));
    }
}
