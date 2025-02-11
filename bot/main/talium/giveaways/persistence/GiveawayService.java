package talium.giveaways.persistence;

import org.springframework.stereotype.Service;
import talium.giveaways.Giveaway;

@Service
public class GiveawayService {

    public void save(Giveaway giveaway) {
        var g = new GiveawayDAO(giveaway);

    }
}
