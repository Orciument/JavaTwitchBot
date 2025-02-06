package talium.inputs.Twitch4J;

import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TwitchAPINoop implements TwitchApi {

    Logger logger = LoggerFactory.getLogger(TwitchApiImpl.class);

    private void logNoOp() {
        logger.warn("Tried to make a api call the twitch API. bevor the api was initialized:", new Exception("Call to no-op twitch api"));
    }

    @Override
    public boolean isPlaceHolder() {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        logNoOp();
    }

    @Override
    public Optional<User> getUserById(String userId) {
        logNoOp();
        return Optional.empty();
    }

    @Override
    public List<User> getUserById(List<String> userId) {
        logNoOp();
        return List.of();
    }

    @Override
    public Optional<User> getUserByName(String username) {
        logNoOp();
        return Optional.empty();
    }

    @Override
    public List<Chatter> getUserList() {
        logNoOp();
        return List.of();
    }

    @Override
    public boolean isOnline() {
        logNoOp();
        return false;
    }
}
