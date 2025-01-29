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

import java.util.List;
import java.util.regex.Pattern;

/// Used as a central place to register actions and resources with parts of the bot
@Component
public class Registrar {

    private static final ChatCooldown DEFAULT_COOLDOWN = new ChatCooldown(CooldownType.MESSAGES, 0);
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

    /// Registers an automatically generated command with a callback
    public static void registerCallbackCommand(RuntimeTrigger command) {
        TriggerProvider.addCommandRegistration(command);
    }

    /// Registers an automatically generated command with a callback
    public static void registerCallbackCommand(String commandId, String pattern, TriggerCallback callback) {
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, List.of(Pattern.compile(pattern)), TwitchUserPermission.EVERYONE, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, callback));
    }

    /// Registers an automatically generated command with a callback
    public static void registerCallbackCommand(String commandId, List<String> pattern, TriggerCallback callback) {
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, pattern.stream().map(Pattern::compile).toList(), TwitchUserPermission.EVERYONE, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, callback));
    }


    /// Registers an automatically generated command without a callback, but with a template
    public static void registerTextCommand(String commandId, String pattern, String template) {
        templateService.saveIfAbsent(new Template(commandId, template, null));
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, List.of(Pattern.compile(pattern)), TwitchUserPermission.EVERYONE, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, TriggerEngine.TEXT_COMMAND_CALLBACK));
    }
    
    /// Registers an automatically generated command without a callback, but with a template
    public static void registerTextCommand(String commandId, List<String> pattern, String template) {
        templateService.saveIfAbsent(new Template(commandId, template, null));
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, pattern.stream().map(Pattern::compile).toList(), TwitchUserPermission.EVERYONE, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, TriggerEngine.TEXT_COMMAND_CALLBACK));
    }

    /// Registers an automatically generated command without a callback, but with a template
    public static void registerTextCommand(String commandId, List<String> pattern, TwitchUserPermission permission, String template) {
        templateService.saveIfAbsent(new Template(commandId, template, null));
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, pattern.stream().map(Pattern::compile).toList(), permission, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, TriggerEngine.TEXT_COMMAND_CALLBACK));
    }

    /// Registers an automatically generated command without a callback, but with a template
    public static void registerTextCommand(String commandId, List<String> pattern, TwitchUserPermission permission, String template, String messageColor) {
        templateService.saveIfAbsent(new Template(commandId, template, messageColor));
        TriggerProvider.addCommandRegistration(new RuntimeTrigger(commandId, pattern.stream().map(Pattern::compile).toList(), permission, DEFAULT_COOLDOWN, DEFAULT_COOLDOWN, TriggerEngine.TEXT_COMMAND_CALLBACK));
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
