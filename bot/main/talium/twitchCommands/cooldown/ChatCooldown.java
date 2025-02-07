package talium.twitchCommands.cooldown;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Embeddable;

/**
 * A Cooldown with an amount of some unit. <br/>
 * E.g.: <br/>
 * - 4 MESSAGES <br/>
 * - 27 SECONDS
 */
@Embeddable
public class ChatCooldown {
    CooldownType type;
    int amount;

    /**
     * @param type the unit of the amount value
     * @param amount       amount of the unit CooldownType
     */
    public ChatCooldown(
            @Nonnull CooldownType type,
            int amount
    ) {
        this.type = type;
        this.amount = amount;
    }

    protected ChatCooldown() {

    }

    @Override
    public String toString() {
        return STR."ChatCooldown{type=\{type}, amount=\{amount}\{'}'}";
    }
}
