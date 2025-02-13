package talium.giveaways.transit;

import java.util.Optional;

public record WinnerDTO (
    String userId,
    boolean rejected,
    Optional<String> comment
) { }
