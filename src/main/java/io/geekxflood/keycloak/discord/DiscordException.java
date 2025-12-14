package io.geekxflood.keycloak.discord;

public class DiscordException extends Exception {

    private final int statusCode;

    public DiscordException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public DiscordException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

