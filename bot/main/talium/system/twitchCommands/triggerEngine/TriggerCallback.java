package talium.system.twitchCommands.triggerEngine;

import talium.Twitch4J.ChatMessage;

/**
 * Defies the functions that are valid as triggerCallbacks. <br/>
 * Used in {@link TriggerEngine}
 */
public interface TriggerCallback {
    /**
     * The Callback function
     * @param triggerId the triggerId of the trigger that triggered the callback
     * @param message the message object that satisfied the trigger conditions
     */
    void triggerCallback(String triggerId, ChatMessage message);
}
