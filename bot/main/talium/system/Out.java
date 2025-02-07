package talium.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import talium.Twitch4J.TwitchAPINoop;
import talium.Twitch4J.TwitchApi;
import talium.system.stringTemplates.Template;
import talium.system.stringTemplates.TemplateService;
import talium.system.templateParser.exeptions.*;
import talium.system.templateParser.TemplateParser;
import talium.system.templateParser.statements.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static talium.system.templateParser.TemplateInterpreter.populate;

@Component
public class Out {

    private static TemplateService templateService;

    @Autowired
    public void setTemplateService(TemplateService templateService) {
        Out.templateService = templateService;
    }

    public static class Twitch {

        @NonNull
        public static TwitchApi api = new TwitchAPINoop();

        public static void sendRawMessage(String message) {
            api.sendMessage(message);
        }

        public static String sendNamedTemplate(String id, HashMap<String, Object> baseValues) throws NoSuchElementException {
            Optional<Template> template = templateService.getTemplateById(id);;
            if (template.isEmpty()) {
                throw new NoSuchElementException(STR."no template found for id: \{id}");
                //TODO emit error as event
                //edit: ^^ not sure why we would need to do this. Just output error into console/webconsole clearly our caller has no fucking idea what they want, so we shouldn't even throw. There is no way they could fix this
            }
            //TODO resolve additional Contexts
            return sendRawTemplate(template.get().template, baseValues);
        }

        public static String sendRawTemplate(String template, HashMap<String, Object> values) {
            String message;
            List<Statement> parsed;
            try {
                parsed = new TemplateParser(template).parse();
            } catch (ParsingException e) {
                // TODO display exceptions, but this should not throw exceptions, since a parsing check should be done on save, so it should still work here
                throw new RuntimeException(e);
            }
            try {
                message = populate(parsed, values);
            } catch (InterpretationException e) {
                //TODO handle exceptions
                // the exceptions should be displayed in the console and in the webconsole with a fairly high priority
                throw new RuntimeException(e);
            }
            api.sendMessage(message);
            return message;
        }
    }

    public static class Discord {
    }

    public static class Alert {
    }

    /**
     * Used for errors that should be displayed via a popup message in the panel
     */
    public static class MajorError {
    }

    /**
     * Errors important enough to warrant email alerting
     */
    public static class CriticalError {
    }
}
