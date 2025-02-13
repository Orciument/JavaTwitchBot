package talium.giveaways.transit;


import talium.giveaways.GiveawayStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record GiveawayDTO (
    long id,
    String title,
    String notes,
    Instant createdAt,
    Instant lastUpdatedAt,
    GiveawayStatus status,
    String commandId,
    String commandFirstPattern,
    String commandTemplate,
    Optional<Instant> autoStart,
    Optional<Instant> autoEnd,
    int ticketCost,
    int maxTickets,
    boolean allowRedrawOfUser,
    boolean autoAnnounceWinner,
    List<EntriyDTO> ticketList,
    List<WinnerDTO> winners
) {}
