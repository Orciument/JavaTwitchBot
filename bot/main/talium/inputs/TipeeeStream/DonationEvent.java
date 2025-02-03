package talium.inputs.TipeeeStream;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Currency;

@Entity
//Repo for this Class has nativ Queries, the Table Name musst be changed Manually!
@Table(name = "goals-donation_log")
@Component
public class DonationEvent {
    @Id
    long id;
    //The symbol (and label) could be enough, if it works, but i don't think it will. The Code would enable conversions
    //the symbol and label give options
    public Currency currency;
    public double amount;
    public String message;
    public boolean hasMessage;
    public String tipeee_username;
    public ZonedDateTime donated_at;

    public DonationEvent() {
    }

    public DonationEvent(long id, Currency currency, double amount, String message, boolean hasMessage, String tipeee_username, ZonedDateTime donated_at) {
        this.id = id;
        this.currency = currency;
        this.amount = amount;
        this.message = message;
        this.hasMessage = hasMessage;
        this.tipeee_username = tipeee_username;
        this.donated_at = donated_at;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        String banner = "===================-DONATION " + id + "-===================";
        str.append(banner).append("\n")
                .append("created_at = ").append(donated_at).append("\n")
                .append("from = ").append(tipeee_username).append("\n")
                .append("donation = ").append(amount).append(currency.getSymbol()).append("\n");
        if (hasMessage)
            str.append("message = ").append(message).append("\n");
        str.append("=".repeat(banner.length()));

        return str.toString();
    }
}
