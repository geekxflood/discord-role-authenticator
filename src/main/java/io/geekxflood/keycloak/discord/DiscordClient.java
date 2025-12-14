package io.geekxflood.keycloak.discord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiscordClient {

    private static final String API_BASE = "https://discord.com/api/v10";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String botToken;

    public DiscordClient(String botToken) {
        this.botToken = botToken;
    }

    public List<String> getUserRoles(String guildId, String userId) throws DiscordException {
        String url = String.format("%s/guilds/%s/members/%s", API_BASE, guildId, userId);

        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "Bot " + botToken);
        request.setHeader("Content-Type", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(request)) {

            int status = response.getCode();
            String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

            if (status == 200) {
                JsonNode json = MAPPER.readTree(body);
                JsonNode rolesNode = json.get("roles");

                List<String> roles = new ArrayList<>();
                if (rolesNode != null && rolesNode.isArray()) {
                    for (JsonNode role : rolesNode) {
                        roles.add(role.asText());
                    }
                }
                return roles;

            } else {
                throw new DiscordException("Discord API error: " + status, status);
            }

        } catch (IOException | org.apache.hc.core5.http.ParseException e) {
            throw new DiscordException("Failed to connect to Discord API", 0, e);
        }
    }
}
