package talium.security.auth.persistence;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Primary user object, holds User Preferences and other User specific, but not security critical Account Information
 */
@Entity
@Table(name = "sys_paneluser")
public class PanelUser {
    @Id
    public String twitchUserId;
    public Instant accountCreationTime;

    public PanelUser() {
    }

    public PanelUser(String twitchUserId) {
        this.twitchUserId = twitchUserId;
        this.accountCreationTime = Instant.now();
    }

    @Override
    public String toString() {
        return STR."PanelUser{twitchUserId='\{twitchUserId}', creationTime=\{accountCreationTime}";
    }
}
