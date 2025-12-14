package io.geekxflood.keycloak.discord;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Map;

public class DiscordRoleAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(DiscordRoleAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        String guildId = config.get("guildId");
        String requiredRoles = config.get("requiredRoles");
        String botToken = config.get("botToken");

        if (guildId == null || requiredRoles == null || botToken == null) {
            LOG.error("Discord authenticator not properly configured");
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            return;
        }

        UserModel user = context.getUser();
        String discordUserId = user.getFirstAttribute("discord_user_id");

        if (discordUserId == null) {
            LOG.warnf("User %s missing discord_user_id attribute", user.getUsername());
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        try {
            DiscordClient client = new DiscordClient(botToken);
            List<String> userRoles = client.getUserRoles(guildId, discordUserId);

            String[] required = requiredRoles.split(",");
            boolean hasRole = false;

            for (String roleId : required) {
                if (userRoles.contains(roleId.trim())) {
                    hasRole = true;
                    break;
                }
            }

            if (hasRole) {
                LOG.infof("User %s authenticated with Discord role", user.getUsername());
                context.success();
            } else {
                LOG.warnf("User %s lacks required Discord role", user.getUsername());
                context.failure(AuthenticationFlowError.INVALID_USER);
            }

        } catch (DiscordException e) {
            LOG.errorf(e, "Discord API error for user %s", user.getUsername());
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute("discord_user_id") != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // No required actions
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
