package talium.giveaways.persistence;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "giveawayEntries")
@IdClass(EntriesDAO.EntriesId.class)
class EntriesDAO {
    public static class EntriesId {
        GiveawayDAO giveaway;
        String userId;

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof EntriesId entriesId)) return false;

            return Objects.equals(giveaway, entriesId.giveaway) && Objects.equals(userId, entriesId.userId);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(giveaway);
            result = 31 * result + Objects.hashCode(userId);
            return result;
        }
    }
    @Id @ManyToOne(fetch = FetchType.EAGER)
    GiveawayDAO giveaway;
    @Id
    String userId;
    int tickets;

    protected EntriesDAO() { }

    protected EntriesDAO(GiveawayDAO giveaway, String userId, int tickets) {
        this.giveaway = giveaway;
        this.userId = userId;
        this.tickets = tickets;
    }
}
