package talium.security.auth.persistence;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Holds information about the active sessions of each user.
 * Is used to authenticate incoming requests
 */
public class Session {
    public final String accessToken;
    public final String userAgent;
    public Instant lastRefreshedAt;
    public final PanelUser panelUser;

    public Session(String accessToken, String userAgent, Instant lastRefreshedAt, @NotNull PanelUser panelUser) {
        this.accessToken = accessToken;
        this.userAgent = userAgent;
        this.lastRefreshedAt = lastRefreshedAt;
        this.panelUser = panelUser;
    }
}
