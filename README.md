# Discord Role Authenticator for Keycloak

A Keycloak authenticator plugin that validates Discord server roles during authentication.

## Features

- Validates Discord server roles during Keycloak authentication
- Configurable guild ID and required roles
- Support for multiple roles (OR logic)
- Comprehensive error handling

## Requirements

- Keycloak 23.0.7+
- Java 17+
- Discord bot with Server Members Intent
- Maven 3.6+ (for building)

## Quick Start

### 1. Create Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create new application
3. Add bot and enable "Server Members Intent"
4. Copy bot token
5. Invite bot to your server with "View Server Members" permission

### 2. Get IDs

Enable Developer Mode in Discord (Settings → Advanced → Developer Mode):

- **Guild ID**: Right-click server → Copy ID
- **Role ID**: Server Settings → Roles → Right-click role → Copy ID
- **User ID**: Right-click username → Copy ID

### 3. Build

```bash
mvn clean package
```

### 4. Deploy

```bash
cp target/discord-role-authenticator-1.0.0.jar /opt/keycloak/providers/
/opt/keycloak/bin/kc.sh build
/opt/keycloak/bin/kc.sh start
```

### 5. Configure

1. **Add user attribute**:
   - Users → [User] → Attributes
   - Key: `discord_user_id`
   - Value: [Discord User ID]

2. **Create authentication flow**:
   - Authentication → Flows → Duplicate "browser"
   - Add execution: "Discord Role Verification"
   - Configure:
     - Discord Guild ID: [Your Guild ID]
     - Required Role IDs: [Role ID(s), comma-separated]
     - Bot Token: [Your Bot Token]
   - Set to REQUIRED

3. **Bind flow**:
   - Bindings → Browser Flow → Select your flow

## Configuration

### Multiple Roles

```text
Required Role IDs: 123456789,987654321,555555555
```

User needs at least ONE of these roles.

### User Attribute

Users must have `discord_user_id` attribute set to their Discord user ID.

## Error Messages

- `discord_id_missing`: User lacks discord_user_id attribute
- `discord_role_missing`: User doesn't have required role
- `discord_not_in_server`: User not in Discord server

## Troubleshooting

### User not in server

- Verify user is member of Discord server
- Check Guild ID is correct

### Bot permission errors

- Enable "Server Members Intent" in Discord Developer Portal
- Verify bot is in server
- Check bot has "View Server Members" permission

### Missing role

- Verify Role ID is correct (not role name)
- Check user actually has the role in Discord

## Docker Deployment

```dockerfile
FROM quay.io/keycloak/keycloak:23.0.7
COPY discord-role-authenticator-1.0.0.jar /opt/keycloak/providers/
RUN /opt/keycloak/bin/kc.sh build
```

## Kubernetes Deployment

```yaml
apiVersion: k8s.keycloak.org/v2alpha1
kind: Keycloak
metadata:
  name: keycloak
spec:
  unsupported:
    podTemplate:
      spec:
        containers:
          - name: keycloak
            volumeMounts:
              - name: discord-auth
                mountPath: /opt/keycloak/providers/discord-role-authenticator-1.0.0.jar
                subPath: discord-role-authenticator-1.0.0.jar
        volumes:
          - name: discord-auth
            configMap:
              name: discord-authenticator
```

## License

MIT License - see LICENSE file

## Support

- GitHub Issues: Report bugs and request features
- Check Keycloak logs for detailed error messages
