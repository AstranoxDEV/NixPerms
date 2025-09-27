package de.syntaxjason.nixperms.perms;

import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.IPermissionCheck;

import java.util.Objects;

public final class DefaultPermissionCheck implements IPermissionCheck {

    @FunctionalInterface
    public interface PermissionTester {
        boolean test(String node);
    }

    private final PermissionTester tester;

    public DefaultPermissionCheck(PermissionTester tester) {
        this.tester = Objects.requireNonNull(tester, "tester");
    }

    @Override
    public boolean check(IPermission permission) {
        if (permission == null) return false;
        String node = permission.name();
        if (node == null || node.isEmpty()) return false;
        return tester.test(node);
    }

    @Override
    public boolean checkAny(IPermission... permissions) {
        if (permissions == null || permissions.length == 0) return false;
        for (IPermission permission : permissions) {
            if (permission == null) continue;
            if (check(permission)) return true;
        }
        return false;
    }

    @Override
    public boolean checkAll(IPermission... permissions) {
        if (permissions == null || permissions.length == 0) return false;
        for (IPermission permission : permissions) {
            if (permission == null) return false;
            if (!check(permission)) return false;
        }
        return true;
    }
}
