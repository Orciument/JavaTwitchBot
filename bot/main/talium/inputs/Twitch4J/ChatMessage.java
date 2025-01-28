package talium.inputs.Twitch4J;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventUser;
import org.jetbrains.annotations.Nullable;
import talium.system.twitchCommands.cooldown.CooldownService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static talium.inputs.Twitch4J.TwitchUserPermission.*;
import static talium.inputs.Twitch4J.TwitchUserPermission.OWNER;

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
        @Nullable String getCustomRewardId,
        @Nullable String replyToMessageID,
        String channelID,
        Instant sendAT
) {

    public static ChatMessage fromChannelMessageEvent(ChannelMessageEvent event) {
        //Convert ChatMessage
        EventUser eUser = event.getUser();
        TwitchUser user = new TwitchUser(eUser.getId(), eUser.getName(), convertUserPermissions(event.getPermissions()), event.getSubscriberMonths(), event.getSubscriptionTier());
        String replyToID = null;
        if (event.getReplyInfo() != null)
            replyToID = event.getReplyInfo().getMessageId();
        return new ChatMessage(
                event.getMessageEvent().getEventId(),
                CooldownService.computeMessageUserIndex(user),
                CooldownService.computeMessageGlobalIndex(),
                event.getMessage(),
                user,
                event.isHighlightedMessage(),
                event.isHighlightedMessage(),
                event.isDesignatedFirstMessage(),
                event.isUserIntroduction(),
                event.getCustomRewardId().orElse(null),
                replyToID,
                event.getChannel().getId(),
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
                //PRIME_TURBO, NO_VIDEO, MOMENTS, NO_AUDIO, TWITCHSTAFF, SUBGIFTER, BITS_CHEERER, PARTNER, FORMER_HYPE_TRAIN_CONDUCTOR, CURRENT_HYPE_TRAIN_CONDUCTOR -> {}
                default -> {
                }
            }
        }
        return newPerm;
    }

}
