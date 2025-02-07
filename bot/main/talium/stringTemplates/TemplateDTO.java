package talium.stringTemplates;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

public record TemplateDTO(
        String id,
        String template,
        String messageColor,
        String varJsonSchema
) {

    public TemplateDTO(Template template) {
        this(template.id, template.template, template.messageColor, "");
    }

    private static String buildJsonSchema(HashMap<String, Class<?>> vars) {
        StringBuilder varJsonSchema = new StringBuilder("{");
        for (var var : vars.entrySet()) {
            varJsonSchema.append(STR."\"\{var.getKey()}\":\{generateSchema(var.getValue())}, ");
        }
        varJsonSchema.deleteCharAt(varJsonSchema.length() - 1);
        varJsonSchema.deleteCharAt(varJsonSchema.length() - 1);
        varJsonSchema.append("}");
        return varJsonSchema.toString();
    }

    private static String generateSchema(Class<?> clazz) {
        if (clazz.isPrimitive()
                || clazz == String.class
                || clazz == Character.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == Boolean.class
        ) {
            return STR."\"\{clazz.getCanonicalName()}\"";
        }

        if (clazz.isArray()) {
            return STR."[\{generateSchema(clazz.componentType())}]";
        }

        if (Iterable.class.isAssignableFrom(clazz)) {
            return "[...]";
        }

        if (clazz.isEnum()) {
            var variants = Arrays
                    .stream(clazz.getDeclaredFields())
                    .filter(Field::isEnumConstant)
                    .map(Field::getName)
                    .toList();
            String variantsString = variants.toString();
            var variantsStringFormatted = variantsString.substring(1, variantsString.length() - 1);
            return STR."\"(\{variantsStringFormatted})\"";
        }

        StringBuilder schema = new StringBuilder("{");
        for (var var : clazz.getDeclaredFields()) {
            schema.append(STR."\"\{var.getName()}\":\{generateSchema(var.getType())}, ");
        }
        if (clazz.getDeclaredFields().length > 0) {
            schema.deleteCharAt(schema.length() - 1);
            schema.deleteCharAt(schema.length() - 1);
        }
        return schema.append("}").toString();
    }

    public Template toTemplate() {
        return new Template(this);
    }
}
