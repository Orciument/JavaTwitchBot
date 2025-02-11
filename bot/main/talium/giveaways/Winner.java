package talium.giveaways;

import java.util.Optional;

public class Winner {
    String userId;
    boolean rejected;
    Optional<String> comment;

    public String userId() {
        return userId;
    }

    public boolean rejected() {
        return rejected;
    }

    public Optional<String> comment() {
        return comment;
    }
}
