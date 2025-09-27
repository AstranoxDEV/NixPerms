package de.syntaxjason.nixperms.user;

import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermissionCheck;
import de.syntaxjason.nixperms.api.user.IUser;
import de.syntaxjason.nixperms.group.SimplePermissionGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SimpleUser implements IUser {

    public interface UserDataCalculator {
        Object calculate(IUser user);
    }

    private final UUID uniqueId;
    private String name;
    private IPermissionGroup primaryGroup;
    private IPermissionGroup secondaryGroup;
    private final IPermissionCheck permissionCheck;
    private final Map<String, String> meta = new HashMap<>();

    private volatile Object effectiveData;
    private final UserDataCalculator calculator;

    public SimpleUser(UUID uniqueId, String name, IPermissionGroup primaryGroup, IPermissionCheck permissionCheck, UserDataCalculator calculator) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("uniqueId must not be null");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        if(primaryGroup == null) {
            primaryGroup = new SimplePermissionGroup("default");
        }
        if(permissionCheck == null) {
            permissionCheck = null;
        }
        this.uniqueId = uniqueId;
        this.name = name;
        this.primaryGroup = primaryGroup;
        this.permissionCheck = permissionCheck;
        this.calculator = calculator;

        recalculateData();
    }

    @Override
    public UUID uniqueId() {
        return uniqueId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public IPermissionCheck permissions() {
        return permissionCheck;
    }

    @Override
    public IPermissionGroup primaryGroup() {
        return primaryGroup;
    }

    @Override
    public void primaryGroup(IPermissionGroup group) {
        if (group == null) {
            return;
        }
        primaryGroup = group;
        if (secondaryGroup != null && secondaryGroup.uniqueId().equals(group.uniqueId())) {
            secondaryGroup = null;
        }
        recalculateData();
    }

    @Override
    public IPermissionGroup secondaryGroup() {
        return secondaryGroup;
    }

    @Override
    public void secondaryGroup(IPermissionGroup group) {
        if (group == null) {
            return;
        }
        if (primaryGroup != null && primaryGroup.uniqueId().equals(group.uniqueId())) {
            secondaryGroup = null;
            recalculateData();
            return;
        }
        secondaryGroup = group;
        recalculateData();
    }

    @Override
    public void clearSecondaryGroup() {
        if (secondaryGroup == null) {
            return;
        }
        secondaryGroup = null;
        recalculateData();
    }

    @Override
    public String prefix() {
        Optional<String> userPrefix = meta("prefix");
        if (userPrefix.isPresent()) {
            return userPrefix.get();
        }
        if (primaryGroup != null && primaryGroup.prefix() != null) {
            return primaryGroup.prefix();
        }
        if (secondaryGroup != null && secondaryGroup.prefix() != null) {
            return secondaryGroup.prefix();
        }
        return null;
    }

    @Override
    public String suffix() {
        Optional<String> userSuffix = meta("suffix");
        if (userSuffix.isPresent()) {
            return userSuffix.get();
        }
        if (primaryGroup != null && primaryGroup.suffix() != null) {
            return primaryGroup.suffix();
        }
        if (secondaryGroup != null && secondaryGroup.suffix() != null) {
            return secondaryGroup.suffix();
        }
        return null;
    }

    @Override
    public boolean meta(String key, String value) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        meta.put(key, value);
        recalculateData();
        return true;
    }

    @Override
    public boolean removeMeta(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        boolean removed = meta.remove(key) != null;
        if (removed) {
            recalculateData();
        }
        return removed;
    }

    @Override
    public Optional<String> meta(String key) {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.get(key));
    }

    @Override
    public Map<String, String> allMeta() {
        return java.util.Collections.unmodifiableMap(meta);
    }

    public void recalculateData() {
        if (calculator == null) {
            return;
        }
        effectiveData = calculator.calculate(this);
    }

    public Object effectiveData() {
        return effectiveData;
    }
}
