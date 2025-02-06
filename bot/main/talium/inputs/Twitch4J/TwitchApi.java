package talium.inputs.Twitch4J;

import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.User;

import java.util.List;
import java.util.Optional;

public interface TwitchApi {

    boolean isPlaceHolder();

    void sendMessage(String message);

    Optional<User> getUserById(String userId);

    List<User> getUserById(List<String> userId);

    Optional<User> getUserByName(String username);

    List<Chatter> getUserList();

    boolean isOnline();

}
