package io.geekxflood.keycloak.discord;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class DiscordRoleAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "discord-role-authenticator";
    private static final DiscordRoleAuthenticator SINGLETON = new DiscordRoleAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Discord Role Verification";
    }

    @Override
    public String getHelpText() {
        return "Validates user has required Discord server roles";
    }

    @Override
    public String getReferenceCategory() {
        return "discord";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty guildId = new ProviderConfigProperty(
                "guildId",
                "Discord Guild ID",
                "The Discord server ID",
                ProviderConfigProperty.STRING_TYPE,
                null
        );

        ProviderConfigProperty requiredRoles = new ProviderConfigProperty(
                "requiredRoles",
                "Required Role IDs",
                "Comma-separated Discord role IDs",
                ProviderConfigProperty.STRING_TYPE,
                null
        );

        ProviderConfigProperty botToken = new ProviderConfigProperty(
                "botToken",
                "Bot Token",
                "Discord bot token",
                ProviderConfigProperty.PASSWORD,
                null
        );
        botToken.setSecret(true);

        return Arrays.asList(guildId, requiredRoles, botToken);
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}

