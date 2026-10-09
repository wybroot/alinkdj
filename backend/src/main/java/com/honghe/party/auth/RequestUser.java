package com.honghe.party.auth;

import com.honghe.party.entity.SysUser;
import java.util.Set;

public record RequestUser(SysUser user, Set<String> roles) {
    public static final String ATTRIBUTE = "party.authenticatedUser";
    public boolean admin() { return roles.contains("SYS_ADMIN"); }
    public boolean managesNotices() { return admin() || roles.contains("GENERAL_BRANCH_ADMIN"); }
}
