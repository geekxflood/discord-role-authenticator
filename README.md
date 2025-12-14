# Discord Role Authenticator for Keycloak

A Keycloak Service Provider Interface (SPI) plugin that validates whether users have specific roles on a Discord server during authentication. This allows you to restrict access to Keycloak-protected applications based on Discord server membership and roles.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Development](#development)
- [License](#license)

## Features

- ✅ **Discord Role Validation** - Verify users have required Discord server roles during authentication
- ✅ **Multiple Role Support** - Configure multiple acceptable roles (OR logic)
- ✅ **Flexible Integration** - Works with any Keycloak authentication flow
- ✅ **Comprehensive Error Handling** - Clear error messages for various failure scenarios
- ✅ **Production Ready** - Proper logging, exception handling, and security practices
- ✅ **Easy Configuration** - Simple admin console configuration interface

## Requirements

- **Keycloak**: Version 23.0.7 or later
- **Java**: JDK 17 or later
- **Maven**: 3.6+ (for building from source)
- **Discord Bot**: With Server Members Intent enabled
- **Discord Server**: Where you want to validate roles

## Installation

### Step 1: Create Discord Bot

1. Navigate to the [Discord Developer Portal](https://discord.com/developers/applications)
2. Click **"New Application"** and give it a name (e.g., "Keycloak Authenticator")
3. Go to the **"Bot"** section in the left sidebar
4. Click **"Add Bot"** and confirm
5. Under **"Privileged Gateway Intents"**, enable **"Server Members Intent"**
6. Click **"Reset Token"** and copy the bot token (save it securely - you'll need it later)

### Step 2: Invite Bot to Your Server

1. In the Discord Developer Portal, go to **OAuth2 → URL Generator**
2. Select the following scopes:
   - `bot`
3. Select the following bot permissions:
   - `View Server Members`
4. Copy the generated URL and open it in your browser
5. Select your Discord server and authorize the bot

### Step 3: Get Required IDs

Enable Developer Mode in Discord:

1. Open Discord Settings
2. Go to **Advanced**
3. Enable **Developer Mode**

Now you can get the required IDs:

- **Guild (Server) ID**: Right-click your server icon → **Copy ID**
- **Role ID(s)**: Server Settings → Roles → Right-click the role → **Copy ID**
- **User Discord ID**: Right-click your username → **Copy ID**

### Step 4: Build the Plugin

Clone this repository and build the JAR file:

```bash
git clone https://github.com/geekxflood/discord-role-authenticator.git
cd discord-role-authenticator
mvn clean package
```

This creates `discord-role-authenticator-1.0.0.jar` in the `target/` directory.

### Step 5: Deploy to Keycloak

Copy the JAR file to Keycloak's providers directory:

```bash
cp target/discord-role-authenticator-1.0.0.jar /opt/keycloak/providers/
```

Rebuild and restart Keycloak:

```bash
/opt/keycloak/bin/kc.sh build
/opt/keycloak/bin/kc.sh start
```

### Step 6: Verify Installation

Check Keycloak logs for successful provider loading:

```bash
tail -f /opt/keycloak/data/log/keycloak.log
```

Look for a line similar to:

```text
INFO  [org.keycloak.services] KC-SERVICES0047: discord-role-authenticator (io.geekxflood.keycloak.discord.DiscordRoleAuthenticatorFactory) is implementing the internal SPI authenticator
```

## Configuration

### 1. Add Discord User ID to Keycloak Users

Each user must have their Discord user ID stored as a Keycloak user attribute.

**Manual Configuration:**

1. Go to **Users** in the Keycloak Admin Console
2. Select a user
3. Go to the **Attributes** tab
4. Add a new attribute:
   - **Key**: `discord_user_id`
   - **Value**: The user's Discord user ID (e.g., `123456789012345678`)
5. Click **Add** then **Save**

**Automated Configuration:**

You can also map this from an identity provider (e.g., Discord OAuth) or sync from LDAP/user federation.

### 2. Create Authentication Flow

1. In Keycloak Admin Console, go to **Authentication**
2. Click on the **Flows** tab
3. Click **Duplicate** on the "browser" flow
4. Give it a name (e.g., "Browser with Discord Role Check")
5. Click **Add execution**
6. Select **"Discord Role Verification"** from the list
7. Click **Save**

### 3. Configure the Authenticator

1. Click the **⚙️ (Actions)** icon next to "Discord Role Verification"
2. Click **Config**
3. Fill in the configuration:
   - **Alias**: Give it a name (e.g., "Discord Role Check")
   - **Discord Guild ID**: Your Discord server ID
   - **Required Role IDs**: Comma-separated list of Discord role IDs
   - **Bot Token**: Your Discord bot token
4. Click **Save**

**Example Configuration:**

```text
Discord Guild ID: 123456789012345678
Required Role IDs: 987654321098765432,876543210987654321
Bot Token: YOUR_DISCORD_BOT_TOKEN_HERE
```

### 4. Set Execution Requirement

1. In your authentication flow, find "Discord Role Verification"
2. Set the requirement to **REQUIRED** (or **ALTERNATIVE** if you want it as an optional check)

### 5. Bind the Flow

1. Go to the **Bindings** tab
2. Set **Browser Flow** to your new flow (e.g., "Browser with Discord Role Check")
3. Click **Save**

## Usage

### How It Works

1. User attempts to authenticate to a Keycloak-protected application
2. Keycloak executes the authentication flow
3. The Discord Role Authenticator checks if the user has the `discord_user_id` attribute
4. The authenticator queries the Discord API to get the user's roles on the specified server
5. If the user has at least one of the required roles, authentication continues
6. If the user lacks the required role(s), authentication fails with an error message

### Multiple Roles (OR Logic)

You can specify multiple acceptable roles. The user needs to have **at least ONE** of them:

```text
Required Role IDs: 111111111111111111,222222222222222222,333333333333333333
```

This allows users with role `111111111111111111` **OR** `222222222222222222` **OR** `333333333333333333` to authenticate.

### Error Messages

The authenticator provides clear error messages:

- **Missing Discord ID**: User doesn't have `discord_user_id` attribute set
- **Missing Role**: User doesn't have any of the required Discord roles
- **Not in Server**: User is not a member of the Discord server
- **Internal Error**: Configuration error or Discord API failure

## Deployment

### Docker

Create a custom Keycloak image with the plugin:

**Dockerfile:**

```dockerfile
FROM quay.io/keycloak/keycloak:23.0.7

# Copy the provider JAR
COPY discord-role-authenticator-1.0.0.jar /opt/keycloak/providers/

# Build Keycloak with the new provider
RUN /opt/keycloak/bin/kc.sh build

# Set environment variables
ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=change_me

# Start Keycloak
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start-dev"]
```

**Build and run:**

```bash
docker build -t keycloak-discord:23.0.7 .
docker run -p 8080:8080 keycloak-discord:23.0.7
```

### Kubernetes with Keycloak Operator

For Kubernetes deployments using the Keycloak Operator:

**Step 1: Create ConfigMap with the JAR**

```bash
kubectl create configmap discord-authenticator \
  --from-file=discord-role-authenticator-1.0.0.jar \
  -n keycloak
```

**Step 2: Update Keycloak Custom Resource**

```yaml
apiVersion: k8s.keycloak.org/v2alpha1
kind: Keycloak
metadata:
  name: keycloak
  namespace: keycloak
spec:
  instances: 1
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

**Step 3: Apply the configuration**

```bash
kubectl apply -f keycloak.yaml
```

### Kubernetes with Helm

If using a Helm chart for Keycloak:

**values.yaml:**

```yaml
keycloak:
  extraInitContainers: |
    - name: provider-download
      image: curlimages/curl:latest
      command:
        - sh
        - -c
        - |
          curl -L -o /providers/discord-role-authenticator.jar \
            https://github.com/geekxflood/discord-role-authenticator/releases/download/v1.0.0/discord-role-authenticator-1.0.0.jar
      volumeMounts:
        - name: providers
          mountPath: /providers

  extraVolumeMounts: |
    - name: providers
      mountPath: /opt/keycloak/providers

  extraVolumes: |
    - name: providers
      emptyDir: {}
```

**Deploy:**

```bash
helm upgrade --install keycloak codecentric/keycloak -f values.yaml
```

## Troubleshooting

### User Authentication Fails with "Missing Discord ID"

**Problem**: User doesn't have `discord_user_id` attribute.

**Solution**:

1. Go to Keycloak Admin Console → Users
2. Select the user
3. Go to Attributes tab
4. Add `discord_user_id` attribute with the user's Discord ID
5. Save

### User Authentication Fails with "Not in Server"

**Problem**: User is not a member of the Discord server.

**Solution**:

1. Verify the Guild ID is correct
2. Ensure the user has joined the Discord server
3. Check that the bot can see the user in the server

### User Authentication Fails with "Missing Role"

**Problem**: User doesn't have any of the required Discord roles.

**Solution**:

1. Verify the Role ID(s) are correct (use role IDs, not names)
2. Check that the user actually has the role in Discord
3. Ensure the role is assigned properly in Discord server settings

### Bot Permission Errors

**Problem**: Bot lacks permissions to check user roles.

**Solution**:

1. Enable "Server Members Intent" in Discord Developer Portal
2. Verify the bot is a member of the Discord server
3. Ensure the bot has "View Server Members" permission
4. Check that the bot token is valid and correctly configured

### Plugin Not Loading

**Problem**: Keycloak doesn't recognize the authenticator.

**Solution**:

1. Verify the JAR is in `/opt/keycloak/providers/`
2. Ensure you ran `/opt/keycloak/bin/kc.sh build` after copying the JAR
3. Check Keycloak logs for errors during startup
4. Verify the SPI registration file exists in the JAR:

   ```bash
   jar tf discord-role-authenticator-1.0.0.jar | grep AuthenticatorFactory
   ```

### Discord API Errors

**Problem**: Errors communicating with Discord API.

**Solution**:

1. Verify the bot token is correct
2. Check network connectivity from Keycloak to Discord API
3. Review Keycloak logs for detailed error messages
4. Ensure Discord API is not rate-limiting your requests

### Debugging Tips

**Enable Debug Logging:**

Add to Keycloak configuration:

```bash
/opt/keycloak/bin/kc.sh start --log-level=DEBUG
```

**Check Logs:**

```bash
# Standalone Keycloak
tail -f /opt/keycloak/data/log/keycloak.log

# Docker
docker logs -f <container-name>

# Kubernetes
kubectl logs -f deployment/keycloak -n keycloak
```

## Development

### Building from Source

```bash
git clone https://github.com/geekxflood/discord-role-authenticator.git
cd discord-role-authenticator
mvn clean package
```

### Project Structure

```text
discord-role-authenticator/
├── pom.xml                                 # Maven build configuration
├── README.md                               # This file
├── LICENSE                                 # MIT License
└── src/
    └── main/
        ├── java/
        │   └── io/geekxflood/keycloak/discord/
        │       ├── DiscordRoleAuthenticator.java        # Main authenticator
        │       ├── DiscordRoleAuthenticatorFactory.java # SPI factory
        │       ├── DiscordClient.java                   # Discord API client
        │       └── DiscordException.java                # Exception handling
        └── resources/
            └── META-INF/
                └── services/
                    └── org.keycloak.authentication.AuthenticatorFactory
```

### Running Tests

```bash
mvn test
```

### Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/geekxflood/discord-role-authenticator/issues)
- **Discussions**: [GitHub Discussions](https://github.com/geekxflood/discord-role-authenticator/discussions)
- **Documentation**: Check Keycloak logs for detailed error messages

## Acknowledgments

- Built for [Keycloak](https://www.keycloak.org/)
- Uses [Discord API](https://discord.com/developers/docs)
- Inspired by the need for Discord-based access control

---

**Made with ❤️ by [GeekXFlood](https://github.com/geekxflood)**
