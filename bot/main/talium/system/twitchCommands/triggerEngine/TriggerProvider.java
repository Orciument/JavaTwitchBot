package talium.system.twitchCommands.triggerEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import talium.system.twitchCommands.persistence.MessagePattern;
import talium.system.twitchCommands.persistence.TriggerEntity;
import talium.system.twitchCommands.cooldown.ChatCooldown;
import talium.system.twitchCommands.persistence.TriggerService;
import talium.inputs.Twitch4J.ChatMessage;
import talium.system.twitchCommands.cooldown.CooldownType;
import talium.inputs.Twitch4J.TwitchUserPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TriggerProvider {
    //TODO resetable cache

    /**
     * Cache of runtime triggers, should only be build from the db, and needs to be rebuilt on trigger change
     */
    private static List<RuntimeTrigger> triggers;
    private static TriggerService triggerService;
    private static final HashMap<String, RuntimeTrigger> codeTriggerMap = new HashMap<>();

    @Autowired
    public TriggerProvider(TriggerService triggerService) {
        TriggerProvider.triggerService = triggerService;
        triggers = buildTrigger();
    }

    public static List<RuntimeTrigger> triggers() {
        return triggers;
    }


    /**
     * Rebuilds the cache of triggers
     */
    public static void rebuildTriggerCache() {
        triggers = buildTrigger();
    }

    /**
     * Builds the trigger list of triggers defined in code, and triggers form the trigger database.
     * If a particular triggerId is found in the code and the database the callback of the code trigger and
     * the data of the database trigger is used, since it may have been changed by the user.
     * @return a list of processed RuntimeTriggers
     */
    public static List<RuntimeTrigger> buildTrigger() {
        // get list of DB triggers (user commands, and system triggers from last starts)
        HashMap<String, TriggerEntity> dbCodeTriggers = triggerService.getCodeTriggers();

        HashMap<String, RuntimeTrigger> resultMap = triggerService.getUserTriggers();

        // get overlap between sets
        // if triggerId is on both sets, the values from the db are preferred, bot the callback is taken from the code
        // if an triggerId is only in the code not in the db, the full trigger from the code is used
        // if a trigger is only in the db and not in the code the triggerId is removed from the DB, because it is not used anymore
        for (var trigger : codeTriggerMap.values()) {
            if (dbCodeTriggers.containsKey(trigger.id())) {
                // triggerId is in both maps, use DB instance with code callback
                TriggerEntity triggerEntity = dbCodeTriggers.get(trigger.id());
                RuntimeTrigger runtimeTrigger = transformTrigger(triggerEntity, trigger.callback());
                resultMap.put(trigger.id(), runtimeTrigger);
                // remove triggerId from db callbacks so that we can later clean up all unused code callbacks from the DB
                dbCodeTriggers.remove(trigger.id());
            } else {
                // if triggerId is not in DB we use the code instance
                resultMap.put(trigger.id(), trigger);
            }
        }
        dbCodeTriggers.keySet().forEach(key -> triggerService.removeTrigger(key));

        return resultMap.values().stream().toList();
    }

    /**
     * Replaces a trigger with the same triggerId of the new trigger, with the new trigger instance.
     * Used for updates to triggers to edits in the ui
     * @param newTrigger replacement trigger
     * @param callback replacement callback
     */
    public static void updateTrigger(TriggerEntity newTrigger, TriggerCallback callback) {
        for (RuntimeTrigger trigger : triggers) {
            if (trigger.id().equals(newTrigger.id)) {
                triggers.remove(trigger);
                triggers.add(transformTrigger(newTrigger, callback));
                return;
            }
        }
    }

    /**
     * Transforms a Chattrigger into a runtime Trigger for internal use of the triggerEngine.
     *
     * @apiNote Discards all disabled patterns
     * @param trigger  trigger to transform
     * @param callback callback to call on successfull trigger
     * @return transformed trigger
     */
    public static RuntimeTrigger transformTrigger(TriggerEntity trigger, TriggerCallback callback) {
        List<Pattern> regexes = new ArrayList<>();
        for (var pattern : trigger.patterns) {
            if (!pattern.isEnabled) {
                continue;
            }
            if (pattern.isRegex) {
                regexes.add(Pattern.compile(pattern.pattern));
            } else {
                regexes.add(Pattern.compile(STR."^\{pattern.pattern}\\b", Pattern.CASE_INSENSITIVE));
            }
        }
        return new RuntimeTrigger(
                trigger.id,
                regexes,
                trigger.permission,
                trigger.userCooldown,
                trigger.globalCooldown,
                callback
        );
    }

    /**
     * Add the commands are specified in the InputConfiguration to an internal list to use while building the final cache of commands.
     * @param commands list of commands to add
     * @implNote Only to be used by the InputManager
     */
    public static void addCommandsFromCodeConfig(List<RuntimeTrigger> commands) {
        for (RuntimeTrigger command : commands) {
            codeTriggerMap.put(command.id(), command);
            if (triggerService.existsById(command.id())) {
                continue;
            }
            var patterns = command.patterns().stream().map(pattern -> new MessagePattern(pattern.pattern(), true, false, true)).toList();
            TriggerEntity entity = new TriggerEntity(command.id(), "", patterns, command.permission(), command.userCooldown(), command.globalCooldown(), true, null);
            try {
                triggerService.save(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
