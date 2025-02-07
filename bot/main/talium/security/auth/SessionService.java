package talium.security.auth;

import talium.security.auth.persistence.PanelUser;
import talium.security.auth.persistence.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages active session, allowing for creating, querying, and deleting sessions
 */
public class SessionService {
    private static final List<Session> sessions = new ArrayList<>();

    public static void createSession(final Session session) {
        assert session != null;
        sessions.add(session);
    }

    public static void deleteSession(Session session) {
        sessions.remove(session);
    }

    public static void deleteAllSessions() {
        sessions.clear();
    }

    public static Optional<Session> getByAccessToken(String accessToken) {
        return sessions.stream().filter(Objects::nonNull).filter(session -> session.accessToken.equals(accessToken)).findFirst();
    }

    public static Optional<Session> getByPanelUser(PanelUser panelUser) {
        return sessions.stream().filter(Objects::nonNull).filter(session -> session.panelUser.equals(panelUser)).findFirst();
    }
}