package talium.stringTemplates;

import jakarta.persistence.*;
import org.jetbrains.annotations.Nullable;

@Table(name = "sys-string_templates")
@Entity
public class Template {
    public @Id String id;
    @Column(length = 500)
    public String template;
    public @Nullable String messageColor;

    public Template(String id, String template, @Nullable String messageColor) {
        this.id = id;
        this.template = template;
        this.messageColor = messageColor;
    }

    public Template(TemplateDTO templateDTO) {
        this.id = templateDTO.id();
        this.template = templateDTO.template();
        this.messageColor = templateDTO.messageColor();
    }

    protected Template() {

    }

    public TemplateDTO toTemplateDTO() {
        return new TemplateDTO(this);
    }
}
