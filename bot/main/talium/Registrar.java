package talium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import talium.inputs.Twitch4J.TwitchUserPermission;
import talium.system.inputSystem.HealthManager;
import talium.system.stringTemplates.Template;
import talium.system.stringTemplates.TemplateService;
import talium.system.twitchCommands.cooldown.ChatCooldown;
import talium.system.twitchCommands.cooldown.CooldownType;
import talium.system.twitchCommands.triggerEngine.RuntimeTrigger;
import talium.system.twitchCommands.triggerEngine.TriggerCallback;
import talium.system.twitchCommands.triggerEngine.TriggerEngine;
import talium.system.twitchCommands.triggerEngine.TriggerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/// Used as a central place to register actions and resources with parts of the bot
@Component
public class Registrar {

    static TemplateService templateService;

    @Autowired
    public Registrar(TemplateService templateService) {
        Registrar.templateService = templateService;
    }

    public Registrar() {
    }

    /// Register Custom HealthUI titel and description
    public static void registerHealthDescription(String self, String title, String description) {
        HealthManager.addCustomization(self, title, description);
    }

    /// Register Custom HealthUI titel and description
    public static void registerHealthDescription(Class self, String title, String description) {
        HealthManager.addCustomization(self.getSimpleName(), title, description);
    }

    public interface ResetableCache {
        void rebuild();
    }

    /// Register a function to reset/rebuild a application cache from the panel ui
    public static void registerResetableChache(String name, String description, ResetableCache cache) {
        //TODO register cache, show in ui, act on reset HTTP post
    }

    /// Register an autogenerated command with the bot
    public static class Command {
        //set all defaults
        String id;
        List<Pattern> patterns = new ArrayList<>();
        TwitchUserPermission permission = TwitchUserPermission.EVERYONE;
        ChatCooldown userCooldown = new ChatCooldown(CooldownType.MESSAGES, 0);
        ChatCooldown globalCooldown = new ChatCooldown(CooldownType.MESSAGES, 0);

        public Command(String id) {
            checkId(id);
            this.id = id;
        }

        public Command(String id, String pattern) {
            checkId(id);
            this.id = id;
        }

        private static void checkId(String id) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("Command id cannot be null or empty");
            }
            if (id.startsWith("userCommand.")) {
                throw new IllegalArgumentException("Autogenerated command is not allowed to have the prefix \"userCommand.\" cause: '"+ id);
            }
        }

        public Command id(String id) {
            this.id = id;
            return this;
        }

        public Command patterns(List<String> patterns) {
            for (String pattern : patterns) {
                this.patterns.add(Pattern.compile(pattern));
            }
            return this;
        }

        public Command pattern(String pattern) {
            this.patterns.add(Pattern.compile(pattern));
            return this;
        }

        public Command permission(TwitchUserPermission permission) {
            this.permission = permission;
            return this;
        }

        public Command userCooldown(ChatCooldown userCooldown) {
            this.userCooldown = userCooldown;
            return this;
        }

        public Command globalCooldown(ChatCooldown globalCooldown) {
            this.globalCooldown = globalCooldown;
            return this;
        }

        /// Registers an automatically generated command with a callback
        public void registerActionCommand(TriggerCallback callback) {
            TriggerProvider.addCommandRegistration(new RuntimeTrigger(id, patterns, permission, userCooldown,globalCooldown, callback));
        }

        /// Registers an automatically generated command without a callback, but with a template
        public void registerTextCommand(String template) {
            registerTextCommand(template, null);
        }

        /// Registers an automatically generated command without a callback, but with a template
        public void registerTextCommand(String template, String messageColor) {
            templateService.saveIfAbsent(new Template(id, template, messageColor));
            TriggerProvider.addCommandRegistration(new RuntimeTrigger(id, patterns, permission, userCooldown,globalCooldown, TriggerEngine.TEXT_COMMAND_CALLBACK));
        }
    }

    /// Registers a template with the context variables (captured environment)
    public static void registerTemplate(Template template) {
        templateService.saveIfAbsent(template);
    }

    /// Registers a template with the context variables (captured environment)
    public static void registerTemplate(String templateId, String template, String messageColor) {
        templateService.saveIfAbsent(new Template(templateId, template, messageColor));

    }

    /// Registers a template with the context variables (captured environment)
    public static void registerTemplate(String templateId, String template) {
        templateService.saveIfAbsent(new Template(templateId, template, null));
    }
}
