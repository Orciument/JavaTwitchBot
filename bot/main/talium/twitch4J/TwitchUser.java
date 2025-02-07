package talium.twitch4J;

import java.util.Objects;

/**
 * TwitchUser, lediglich dazu gedacht, um einen TwitchUser in einer Chatnachricht zu repräsentieren,
 * und ist nicht dazu gedacht in der Datenbank gespeichert zu werden, denn die Twitch Permissions und Badges könnten
 * sich während der Laufzeit ändern
 */
public final class TwitchUser {
    private final String id;
    private final String name;
    private final TwitchUserPermission permission;
    private final int subscriberMonths;
    private final int subscriptionTier;

    public TwitchUser(
            String id,
            String name,
            TwitchUserPermission permission,
            int subscriberMonths,
            int subscriptionTier
    ) throws ChatMessage.ChatMessageMalformedExceptions {
        if (id == null || id.isEmpty())
            throw new ChatMessage.ChatMessageMalformedExceptions("Twitch User ID cannot be empty");
        this.id = id;
        if (name == null || name.isEmpty())
            throw new ChatMessage.ChatMessageMalformedExceptions("Username cannot be empty");
        this.name = name;
        this.permission = permission;
        if (subscriberMonths < 0 || subscriptionTier < 0)
            throw new ChatMessage.ChatMessageMalformedExceptions("Subscriber Months and Tier are not allowed to be negative");
        this.subscriberMonths = subscriberMonths;
        this.subscriptionTier = subscriptionTier;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public TwitchUserPermission permission() {
        return permission;
    }

    public int subscriberMonths() {
        return subscriberMonths;
    }

    public int subscriptionTier() {
        return subscriptionTier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TwitchUser) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.permission, that.permission) &&
                this.subscriberMonths == that.subscriberMonths &&
                this.subscriptionTier == that.subscriptionTier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, permission, subscriberMonths, subscriptionTier);
    }

    @Override
    public String toString() {
        return "TwitchUser[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "permission=" + permission + ", " +
                "subscriberMonths=" + subscriberMonths + ", " +
                "subscriptionTier=" + subscriptionTier + ']';
    }
}
