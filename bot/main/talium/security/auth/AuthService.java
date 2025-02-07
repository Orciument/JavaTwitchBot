package talium.security.auth;

import kotlin.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import talium.security.auth.persistence.PanelUser;
import talium.security.auth.persistence.PanelUserRepo;
import talium.security.auth.persistence.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static talium.security.auth.SessionService.*;

@Service
public class AuthService {

    private final Duration sessionTimeout = Duration.ofMinutes(15);
    private final PanelUserRepo panelUserRepo;
    public static boolean byPassAllAuth;
    private final List<String> configAllowedUsernames;
    private final boolean overwritePanelUsers;

    @Autowired
    public AuthService(PanelUserRepo panelUserRepo, @Value("${disableAllAuth}") String disableAllAuth, @Value("${allowedPanelUsers}") List<String> configAllowedUsernames, @Value("${overwritePanelUsers:false}") boolean overwritePanelUsers) {
        this.panelUserRepo = panelUserRepo;
        byPassAllAuth = disableAllAuth.equals("true");
        this.configAllowedUsernames = configAllowedUsernames;
        this.overwritePanelUsers = overwritePanelUsers;
    }

    public boolean authenticate(String accessToken, String userAgent) throws AuthenticationRejected {
        Optional<Session> optSession = getByAccessToken(accessToken);
        if (optSession.isPresent()) {
            // if session is not timed out, and the userAgent is the same, we short circuit accepting the token and granting access
            Session session = optSession.get();
            if (!session.userAgent.equals(userAgent)) {
                throw new AuthenticationRejected(HttpStatus.UNAUTHORIZED, "Reauthenticate with access token");
            }
            if (!session.lastRefreshedAt.plusSeconds(sessionTimeout.toSeconds()).isBefore(Instant.now())) {
                deleteSession(session);
                return true;
            }
        }
        // if there is no session, or session is timed out, or not valid for this userAgent we try to check if the accessToken is still valid for twitch
        // if the access token is valid, we create a new session, if it is not valid anymore, we reject the request
        var validation = validateToken(accessToken);
        // no validation means token invalid
        if (validation.isEmpty()) {
            throw new AuthenticationRejected(HttpStatus.UNAUTHORIZED, "Invalid access token, Reauthenticate!");
        }

        if (overwritePanelUsers) {
            if (!configAllowedUsernames.contains(validation.get().component1())) {
                throw new AuthenticationRejected(HttpStatus.FORBIDDEN, STR."User: \{validation.get().component1()} not allowed to access this ressource");
            }
        } else {
            if (!panelUserRepo.existsById(validation.get().component2())) {
                throw new AuthenticationRejected(HttpStatus.FORBIDDEN, STR."User: \{validation.get().component1()} not allowed to access this ressource");
            }
        }

        var butUser = panelUserRepo.findById(validation.get().component2());
        if (butUser.isEmpty()) {
            PanelUser entity = new PanelUser(validation.get().component2());
            panelUserRepo.save(entity);
            butUser = Optional.of(entity);
        }

        createSession(new Session(accessToken, userAgent, Instant.now(), butUser.get()));
        return true;
    }

    public Optional<Pair<String, String>> validateToken(String accessToken) {
        try (var client = HttpClient.newBuilder().build()) {
            URI twitchValidateToken = URI.create("https://id.twitch.tv/oauth2/validate");
            var request = HttpRequest
                    .newBuilder(twitchValidateToken)
                    .setHeader("Authorization", STR."Bearer \{accessToken}")
                    .GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401) {
                return Optional.empty();
            }
            var json = new JSONObject(response.body());
            var userId = json.getString("user_id");
            var userName = json.getString("login");
            return Optional.of(new Pair<>(userName, userId));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
