package talium.system.twitchCommands.persistence;

import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;
import talium.system.stringTemplates.TemplateDTO;
import talium.system.twitchCommands.controller.MessagePatternDTO;
import talium.system.twitchCommands.controller.TriggerDTO;
import talium.system.twitchCommands.cooldown.ChatCooldown;
import talium.system.twitchCommands.triggerEngine.TriggerEngine;
import talium.inputs.Twitch4J.TwitchUserPermission;
import talium.system.stringTemplates.Template;

import java.util.Arrays;
import java.util.List;

//TODO reference triggerId guidelines

/**
 * A Trigger is a set of conditions that a chat message needs to match. If it matches a callback will be executed by the {@link TriggerEngine}. <br/>
 * <br/>
 * ChatTrigger are about half of a Chat Command, the other half being a stringTemplate
 */
@Entity
@Table(name = "sys-chatTrigger-trigger")
public class TriggerEntity {
    @Id
    public String id;
    public String description;
    @OneToMany(mappedBy = "parentTrigger", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public List<MessagePattern> patterns;
    public TwitchUserPermission permission;

    @Embedded @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "userCooldown_type")),
            @AttributeOverride(name = "amount", column = @Column(name = "userCooldown_amount"))
    })
    public ChatCooldown userCooldown;

    @Embedded @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "globalCooldown_type")),
            @AttributeOverride(name = "amount", column = @Column(name = "globalCooldown_amount"))
    })
    public ChatCooldown globalCooldown;
    public boolean isAutoGenerated;

    @Nullable
    @OneToOne
    @JoinColumn(referencedColumnName = "id")
    public Template template;

    /**
     * @param triggerId      a unique triggerId that identifies this trigger. Is not allowed to collide with other triggerIds
     * @param patterns       a list of matcher Patterns that the message content is matched against. It is enough if any of these patterns match
     * @param permission     a minimum permission level a user of the message needs to have
     * @param userCooldown   a user specific cooldown for this trigger
     * @param globalCooldown a global (for all users) cooldown for this trigger
     */
    public TriggerEntity(String triggerId, String description, List<MessagePattern> patterns, TwitchUserPermission permission, ChatCooldown userCooldown, ChatCooldown globalCooldown, boolean isAutoGenerated, Template template) {
        this.id = triggerId;
        this.description = description;
        this.patterns = patterns;
        this.permission = permission;
        this.userCooldown = userCooldown;
        this.globalCooldown = globalCooldown;
        this.isAutoGenerated = isAutoGenerated;
        this.template = template;
    }

    public TriggerEntity(TriggerDTO triggerDTO) {
        this.id = triggerDTO.id();
        this.description = triggerDTO.description();
        this.patterns = Arrays.stream(triggerDTO.patterns()).map(MessagePatternDTO::toMessagePattern).toList();
        this.permission = triggerDTO.permission();
        this.userCooldown = triggerDTO.userCooldown();
        this.globalCooldown = triggerDTO.globalCooldown();
        this.isAutoGenerated = triggerDTO.isAutoGenerated();
        this.template = triggerDTO.template().toTemplate();
    }

    protected TriggerEntity() {

    }

    public TriggerDTO toTriggerDTO() {
        TemplateDTO templateDto = null;
        if (this.template != null) {
            templateDto = template.toTemplateDTO();
        }

        return new TriggerDTO(
                id,
                description,
                patterns.stream().map(MessagePattern::toMessagePatternDTO).toArray(MessagePatternDTO[]::new),
                permission,
                userCooldown,
                globalCooldown,
                isAutoGenerated,
               templateDto
        );
    }
}
