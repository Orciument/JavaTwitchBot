package talium.giveaways;

import talium.twitchCommands.persistence.TriggerEntity;

import java.time.Instant;
import java.util.*;

public class Giveaway {
    private final long id;
    private String title;
    private String notes;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    private GiveawayStatus status;
    private TriggerEntity command;
    private Optional<Instant> autoStart;
    private Optional<Instant> autoEnd;
    private int ticketCost;
    private int maxTickets;
    private boolean allowRedrawOfUser;
    private boolean autoAnnounceWinner;
    private final Map<String, Integer> ticketList = new TreeMap<>();
    private final List<Winner> winners = new ArrayList<>();

    Giveaway(long id, String title, String notes, Instant createdAt, Instant lastUpdatedAt, GiveawayStatus status, TriggerEntity command, Optional<Instant> autoStart, Optional<Instant> autoEnd, int ticketCost, int maxTickets, boolean allowRedrawOfUser, boolean autoAnnounceWinner) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.status = status;
        this.command = command;
        this.autoStart = autoStart;
        this.autoEnd = autoEnd;
        this.ticketCost = ticketCost;
        this.maxTickets = maxTickets;
        this.allowRedrawOfUser = allowRedrawOfUser;
        this.autoAnnounceWinner = autoAnnounceWinner;
    }

    String getCommandTemplate() {
        assert command.template != null;
        return command.template.template;
    }

    String getFirstPattern() {
        return command.patterns.getFirst().pattern;
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String notes() {
        return notes;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant lastUpdatedAt() {
        return lastUpdatedAt;
    }

    public GiveawayStatus status() {
        return status;
    }

    public TriggerEntity command() {
        return command;
    }

    public Optional<Instant> autoStart() {
        return autoStart;
    }

    public Optional<Instant> autoEnd() {
        return autoEnd;
    }

    public int ticketCost() {
        return ticketCost;
    }

    public int maxTickets() {
        return maxTickets;
    }

    public boolean allowRedrawOfUser() {
        return allowRedrawOfUser;
    }

    public boolean autoAnnounceWinner() {
        return autoAnnounceWinner;
    }

    public Map<String, Integer> ticketList() {
        return ticketList;
    }

    public List<Winner> winners() {
        return winners;
    }
}
