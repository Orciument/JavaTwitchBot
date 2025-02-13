package talium.giveaways;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import talium.giveaways.persistence.GiveawayService;
import talium.giveaways.transit.GiveawayDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/giveaway")
public class GiveawayController {
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    GiveawayService giveawayService;

    @Autowired
    public GiveawayController(GiveawayService giveawayService) {
        this.giveawayService = giveawayService;
    }

    @PostMapping
    public HttpStatus create(@RequestBody String body) {
        var chatterDto = gson.fromJson(body, GiveawayDTO.class);
        return HttpStatus.CREATED;
    }

    @GetMapping
    public String getActive() {
        var g = new GiveawayDTO(8328328, "test Title", "", Instant.MIN, Instant.now(), GiveawayStatus.CREATED, "giveaways.test1", "!testGW", "you failed", Optional.empty(), Optional.empty(), 3, 10, false, false, List.of(), List.of());
        return gson.toJson(g);
    }
}
