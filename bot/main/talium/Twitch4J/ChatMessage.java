package talium.Twitch4J;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import talium.system.twitchCommands.cooldown.CooldownService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static talium.Twitch4J.TwitchUserPermission.*;
import static talium.Twitch4J.TwitchUserPermission.OWNER;

public record ChatMessage(
        String messageId,
        int userMessageIndex,
        int globalMessageIndex,
        String message,
        TwitchUser user,
        boolean isHighlightedMessage,
        boolean isSkipSubsModeMessage,
        boolean isDesignatedFirstMessage,
        boolean isUserIntroduction,
        Optional<String> getCustomRewardId,
        Optional<String> replyToMessageID,
        String channelID,
        Instant sendAt
) {
    private static final Logger log = LoggerFactory.getLogger(ChatMessage.class);

    public static class ChatMessageMalformedExceptions extends Exception {
        public ChatMessageMalformedExceptions(String message) {
            super(message);
        }
    }

    public static ChatMessage fromChannelMessageEvent(ChannelMessageEvent event) throws ChatMessageMalformedExceptions {
        //Convert ChatMessage
        EventUser eUser = event.getUser();
        TwitchUser user = new TwitchUser(eUser.getId(), eUser.getName(), convertUserPermissions(event.getPermissions()), event.getSubscriberMonths(), event.getSubscriptionTier());

        String messageId = event.getMessageEvent().getEventId();
        String message = event.getMessage();
        String channelId = event.getChannel().getId();
        if (messageId == null || messageId.isEmpty()) {
            throw new ChatMessageMalformedExceptions("MessageId is not allowed to be empty");
        }
        if (message == null || message.isEmpty()) {
            throw new ChatMessageMalformedExceptions("Message is not allowed to be empty");
        }
        if (channelId == null || channelId.isEmpty()) {
            throw new ChatMessageMalformedExceptions("channelId is not allowed to be empty");
        }

        Optional<String> replyToID = Optional.empty();
        if (event.getReplyInfo() != null)
            replyToID = event.getReplyInfo().getMessageId().describeConstable();

        return new ChatMessage(
                messageId,
                CooldownService.computeMessageUserIndex(user),
                CooldownService.computeMessageGlobalIndex(),
                message,
                user,
                event.isHighlightedMessage(),
                event.isSkipSubsModeMessage(),
                event.isDesignatedFirstMessage(),
                event.isUserIntroduction(),
                event.getCustomRewardId(),
                replyToID,
                channelId,
                event.getFiredAtInstant()
        );
    }

    /**
     * Converts a set of Twitch4J permission into our equivalent Permissions, and returns the highest one.
     * @param userPermissions A set of Twitch4J Permissions
     * @return The highest equivalent ranking permission in our system from the set
     * @see CommandPermission
     * @see TwitchUserPermission
     */
    private static TwitchUserPermission convertUserPermissions(Set<CommandPermission> userPermissions) {
        HashSet<TwitchUserPermission> permissions = translatePerms(userPermissions);
        TwitchUserPermission highest_perm = EVERYONE;
        for (TwitchUserPermission perm : permissions) {
            if (perm.ordinal() > highest_perm.ordinal())
                highest_perm = perm;
        }
        return highest_perm;
    }

    /**
     * Translate the Permissions of a user from Twit4J's permission System into our.
     * @param permissions A Set if Twit4J#s permissions
     * @return A Set of our Permissions
     * @see CommandPermission
     */
    private static HashSet<TwitchUserPermission> translatePerms(Set<CommandPermission> permissions) {
        HashSet<TwitchUserPermission> newPerm = new HashSet<>();
        for (CommandPermission cp : permissions) {
            switch (cp) {
                case EVERYONE -> newPerm.add(EVERYONE);
                case SUBSCRIBER -> newPerm.add(SUBSCRIBER);
                case FOUNDER -> newPerm.add(FOUNDER);
                case PREDICTIONS_BLUE -> newPerm.add(PREDICTIONS_BLUE);
                case PREDICTIONS_PINK -> newPerm.add(PREDICTIONS_PINK);
                case ARTIST -> newPerm.add(ARTIST);
                case VIP -> newPerm.add(VIP);
                case MODERATOR -> newPerm.add(MODERATOR);
                case BROADCASTER -> newPerm.add(BROADCASTER);
                case OWNER -> newPerm.add(OWNER);
                case PRIME_TURBO, NO_VIDEO, MOMENTS, NO_AUDIO, TWITCHSTAFF, SUBGIFTER, BITS_CHEERER, PARTNER, FORMER_HYPE_TRAIN_CONDUCTOR, CURRENT_HYPE_TRAIN_CONDUCTOR -> {}
                default -> log.warn("Unknown user permission: {}", cp);
            }
        }
        return newPerm;
    }

}
