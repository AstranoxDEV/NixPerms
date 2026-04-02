package de.astranox.nixperms.core.sync;

import de.astranox.nixperms.api.message.IMessageService;
import de.astranox.nixperms.api.platform.Platform;
import de.astranox.nixperms.api.sync.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class NixSyncNotifier implements ISyncNotifier {

    private final IMessageService messageService;
    private final Object2ObjectOpenHashMap<Platform, SyncNotificationChannel> channels = new Object2ObjectOpenHashMap<>();

    public NixSyncNotifier(IMessageService messageService) { this.messageService = messageService; }

    @Override
    public void notify(SyncNotification notification) {
        notification.targets().forEach(platform -> { SyncNotificationChannel channel = channels.get(platform); if (channel != null) channel.dispatch(notification, messageService); });
    }

    @Override public void registerChannel(Platform platform, SyncNotificationChannel channel) { channels.put(platform, channel); }
    @Override public void unregisterChannel(Platform platform) { channels.remove(platform); }
}
