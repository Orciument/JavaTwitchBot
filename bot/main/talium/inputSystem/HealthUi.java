package talium.inputSystem;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthUi {

    @GetMapping("/health/json")
    @ResponseBody
    public String healthJson() {
        Gson gson = new Gson();
        return gson.toJson(HealthManager.allStatuses());
    }
}
