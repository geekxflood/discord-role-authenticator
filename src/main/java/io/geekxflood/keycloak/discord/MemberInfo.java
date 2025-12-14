package io.geekxflood.keycloak.discord;

import java.util.List;

/**
 * Holds Discord guild member information including roles and avatar.
 */
public class MemberInfo {
    private final List<String> roles;
    private final String avatarUrl;
    private final String globalAvatarUrl;

    public MemberInfo(List<String> roles, String avatarUrl, String globalAvatarUrl) {
        this.roles = roles;
        this.avatarUrl = avatarUrl;
        this.globalAvatarUrl = globalAvatarUrl;
    }

    public List<String> getRoles() {
        return roles;
    }

    /**
     * Returns the best available avatar URL.
     * Prefers guild-specific avatar, falls back to global avatar.
     */
    public String getAvatarUrl() {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }
        return globalAvatarUrl;
    }

    public String getGuildAvatarUrl() {
        return avatarUrl;
    }

    public String getGlobalAvatarUrl() {
        return globalAvatarUrl;
    }
}
