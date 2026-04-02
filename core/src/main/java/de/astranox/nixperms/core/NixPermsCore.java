package de.astranox.nixperms.core;

import de.astranox.nixperms.api.*;
import de.astranox.nixperms.api.attachment.IAttachmentService;
import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.group.GroupEvent;
import de.astranox.nixperms.api.event.group.GroupPermissionChangeEvent;
import de.astranox.nixperms.api.event.network.NetworkSyncEvent;
import de.astranox.nixperms.api.group.IGroupManager;
import de.astranox.nixperms.api.message.IMessageService;
import de.astranox.nixperms.api.platform.Platform;
import de.astranox.nixperms.api.sync.ISyncNotifier;
import de.astranox.nixperms.api.user.IUserManager;
import de.astranox.nixperms.core.attachment.NixAttachmentService;
import de.astranox.nixperms.core.command.AnnotationCommandProcessor;
import de.astranox.nixperms.core.config.*;
import de.astranox.nixperms.core.database.*;
import de.astranox.nixperms.core.event.NixEventBus;
import de.astranox.nixperms.core.group.NixGroupManager;
import de.astranox.nixperms.core.message.*;
import de.astranox.nixperms.core.message.locale.*;
import de.astranox.nixperms.core.permission.NixPermissionResolver;
import de.astranox.nixperms.core.storage.*;
import de.astranox.nixperms.core.sync.*;
import de.astranox.nixperms.core.user.NixUserManager;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class NixPermsCore implements INixPermsAPI {

    private final NixConfig config;
    private final NixEventBus eventBus;
    private final NixGroupManager groupManager;
    private final NixUserManager userManager;
    private final NixAttachmentService attachmentService;
    private final AnnotationCommandProcessor commandProcessor;
    private final MessageService messageService;
    private final NixSyncNotifier syncNotifier;
    private final SyncMessenger syncMessenger;
    private final SQLDatabase database;

    private NixPermsCore(Path dataFolder) {
        this.config = new NixConfig();
        AnnotationConfigProcessor configProcessor = new AnnotationConfigProcessor(dataFolder.resolve("config.yml"));
        configProcessor.load(config);
        configProcessor.save(config);
        this.database = buildDatabase();
        this.eventBus = new NixEventBus();
        MessageRegistry registry = buildMessageRegistry();
        this.messageService = new MessageService(registry);
        this.syncNotifier = new NixSyncNotifier(messageService);
        Executor dbExecutor = Executors.newVirtualThreadPerTaskExecutor();
        GroupStorageAdapter groupStorage = new GroupStorageAdapter(database, dbExecutor);
        UserStorageAdapter userStorage = new UserStorageAdapter(database, dbExecutor);
        this.groupManager = new NixGroupManager(groupStorage, eventBus);
        NixPermissionResolver resolver = new NixPermissionResolver(groupManager::getChain);
        this.userManager = new NixUserManager(userStorage, groupManager, resolver, eventBus, config.permissions.policy(), dbExecutor);
        this.attachmentService = new NixAttachmentService();
        this.commandProcessor = new AnnotationCommandProcessor(this);
        this.syncMessenger = new SyncMessenger(new SQLSyncStorage(database), eventBus, config.sync.serverId, config.sync.pollIntervalMs);
        registerEventHandlers();
    }

    public static CompletableFuture<NixPermsCore> create(Path dataFolder) {
        NixPermsCore core = new NixPermsCore(dataFolder);
        return core.groupManager.loadAll().thenApply(v -> { core.syncMessenger.start(); return core; });
    }

    public void shutdown() { syncMessenger.stop(); database.disconnect(); }

    public void reload(Path dataFolder) {
        new AnnotationConfigProcessor(dataFolder.resolve("config.yml")).reload(config);
        messageService.registry().setDefaultLocale(config.messages.defaultLocale);
    }

    private void registerEventHandlers() {
        eventBus.subscribe(GroupEvent.class, event -> {
            switch (event) {
                case GroupPermissionChangeEvent e -> { userManager.invalidateGroup(e.group().name()); syncMessenger.publishGroupUpdate(e.group().name()); }
                default -> syncMessenger.publishGroupUpdate(((de.astranox.nixperms.api.event.group.GroupDeleteEvent) event).groupName());
            }
        });
        eventBus.subscribe(NetworkSyncEvent.class, event -> {
            switch (event.syncType()) {
                case USER_UPDATE -> userManager.loadUser(event.affectedUserId());
                case GROUP_UPDATE -> groupManager.reloadGroup(event.affectedGroupName()).thenRun(() -> userManager.invalidateGroup(event.affectedGroupName()));
                case GLOBAL_INVALIDATION -> groupManager.loadAll().thenRun(() -> userManager.loaded().forEach(u -> userManager.invalidateGroup(u.primary().name())));
            }
        });
    }

    private SQLDatabase buildDatabase() {
        SqlDialect dialect = SqlDialect.from(config.database.backend);
        return new SQLDatabase(HikariPoolFactory.create(config.database, dialect), dialect);
    }

    private MessageRegistry buildMessageRegistry() {
        MessageRegistry registry = new MessageRegistry();
        registry.setDefaultLocale(config.messages.defaultLocale);
        registry.register(GroupMessages_en_us.class);
        registry.register(UserMessages_en_us.class);
        registry.register(CommonMessages_en_us.class);
        System.out.println("DEBUG registry.getRaw('en_us', 'BUKKIT', 'commands.group.usage'): " + registry.getRaw("en_us", Platform.BUKKIT, "commands.group.usage"));
        return registry;
    }

    @Override public IUserManager users() { return userManager; }
    @Override public IGroupManager groups() { return groupManager; }
    @Override public IAttachmentService attachments() { return attachmentService; }
    @Override public IEventBus events() { return eventBus; }
    @Override public IMessageService messages() { return messageService; }
    @Override public ISyncNotifier syncNotifier() { return syncNotifier; }
    public NixConfig config() { return config; }
    public AnnotationCommandProcessor commands() { return commandProcessor; }
    public SyncMessenger sync() { return syncMessenger; }
}
