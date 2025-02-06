package talium.inputs.Twitch4J;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.common.exception.UnauthorizedException;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// maybe add no-op version of helix api and just switch them when they are assigned, that way we should be able to remove all these null checking methods
public class TwitchApiImpl implements TwitchApi {
    TwitchHelix helix;
    TwitchChat chat;

    public TwitchApiImpl(TwitchHelix helix, TwitchChat chat) {
        this.helix = helix;
        this.chat = chat;
    }

    // wip: use higher-order-function to reduce boilerplate
    private interface Call<T> {
        T call(TwitchHelix helix);
    }

    private static<T> T doHelixCall(Call<T> call) {
        try {
            return call.call(Twitch4JInput.helix);
        }  catch (UnauthorizedException e) {
            //fix handle missing auth
            return call.call(Twitch4JInput.helix);
        } catch (Exception e) {
            // ehh, log and throw i guess
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPlaceHolder() {
        return false;
    }

    /**
     * Sends a message in the chat specified by the twitchOutputToChannel Env with the bot account specified by the twitchBotAccountName env
     *
     * @param message the message text to send
     */
    @Override
    public void sendMessage(String message) {
        Twitch4JInput.chat.sendMessage(Twitch4JInput.sendTo, message);
    }

    @Override
    public Optional<User> getUserById(String userId) {
        var user =Twitch4JInput.helix.getUsers(null, List.of(userId), null).execute().getUsers();
        if (user.isEmpty()) return Optional.empty();
        return Optional.ofNullable(user.getFirst());
    }

    @Override
    public List<User> getUserById(List<String> userId) {
        return Twitch4JInput.helix.getUsers(null, userId, null).execute().getUsers();
    }

    @Override
    public Optional<User> getUserByName(String username) {
        var user = Twitch4JInput.helix.getUsers(null, null, List.of(username)).execute().getUsers();
        if (user.isEmpty()) return Optional.empty();
        return Optional.ofNullable(user.getFirst());
    }

    @Override
    public List<Chatter> getUserList() {
        OAuth2Credential cred = Twitch4JInput.oAuth2Credential;
        return Twitch4JInput.helix
                .getChatters(cred.getAccessToken(), cred.getUserId(), cred.getUserId(), 1000, null)
                .execute()
                .getChatters();
    }

    @Override
    public boolean isOnline() {
        OAuth2Credential cred = Twitch4JInput.oAuth2Credential;
        return Twitch4JInput.helix.getStreams(cred.getAccessToken(), null, null, null, null, null, null, null).execute().getStreams().size() == 1;
    }

}
