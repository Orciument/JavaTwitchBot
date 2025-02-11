package talium.giveaways.persistence;

import jakarta.persistence.*;
import org.springframework.lang.Nullable;
import talium.giveaways.Giveaway;
import talium.giveaways.GiveawayStatus;
import talium.twitchCommands.persistence.TriggerEntity;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "giveaways")
class GiveawayDAO {
    @Id
    long id;
    OffsetDateTime createdAt;
    OffsetDateTime lastUpdatedAt;
    String title;
    String notes;
    GiveawayStatus status;
    @OneToOne
    @JoinColumn(referencedColumnName = "id")
    TriggerEntity command;
    @Nullable OffsetDateTime autoStart;
    @Nullable OffsetDateTime  autoEnd;
    int ticketCost;
    int maxTickets;
    boolean allowRedrawOfUser;
    boolean autoAnnounceWinner;
    @OneToMany(mappedBy = "giveaway", fetch = FetchType.EAGER)
    List<EntriesDAO> ticketList;
    @OneToMany(mappedBy = "giveaway", fetch = FetchType.EAGER)
    List<WinnersDAO> winners = new ArrayList<>();

    public GiveawayDAO() {
    }

    public GiveawayDAO(Giveaway g) {
        this.id = g.id();
        this.createdAt = OffsetDateTime.from(g.createdAt());
        this.lastUpdatedAt = OffsetDateTime.from(g.createdAt());
        this.title = g.title();
        this.notes = g.notes();
        this.status = g.status();
        this.command = g.command();
        this.autoStart = g.autoStart().map(OffsetDateTime::from).orElse(null);
        this.autoEnd = g.autoEnd().map(OffsetDateTime::from).orElse(null);
        this.ticketCost = g.ticketCost();
        this.maxTickets = g.maxTickets();
        this.allowRedrawOfUser = g.allowRedrawOfUser();
        this.autoAnnounceWinner = g.autoAnnounceWinner();
        this.ticketList = g.ticketList().entrySet().stream().map(e -> new EntriesDAO(this,e.getKey(), e.getValue())).toList();
        this.winners = g.winners().stream().map(w -> new WinnersDAO(this, w.userId(), w.rejected(), w.comment().orElse(null))).toList();
    }


}


