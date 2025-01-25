package talium.system.stringTemplates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/template")
public class TemplateController {

    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/all")
    String getAllCommands(@RequestParam @Nullable String search) {
        if (search == null || search.isEmpty()) {
            var list = templateService.getAllTemplates();
            return gson.toJson(list);
        }
        return gson.toJson(templateService.searchBy(search));
    }

    @PostMapping("/save")
    HttpStatus saveCommand(@RequestBody String body) {
        Template template = gson.fromJson(body, Template.class);
        templateService.save(template);
        return HttpStatus.OK;
    }

    @DeleteMapping("/delete/{id}")
    HttpStatus deleteCommand(@PathVariable String id) {
        if (templateService.getTemplateById(id).isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }
        templateService.delete(id);
        return HttpStatus.OK;
    }
}
