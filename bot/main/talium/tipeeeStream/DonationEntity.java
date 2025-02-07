package talium.tipeeeStream;

import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Currency;

@Entity
@Table(name = "donation_history")
public class DonationEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long donationId;
    public String donationService;
    public String serviceDonationId;
    public String currency_code;
    public long amount_cents;
    public @Nullable String message;
    public String userName;
    public ZonedDateTime donated_at;

    public DonationEntity() {}

    public DonationEntity(String donationService, String serviceDonationId, Currency currency, long amount_cents, @Nullable String message, String userName, ZonedDateTime donated_at) {
        this.donationService = donationService;
        this.serviceDonationId = serviceDonationId;
        this.currency_code = currency.getCurrencyCode();
        this.amount_cents = amount_cents;
        this.message = message;
        this.userName = userName;
        this.donated_at = donated_at;
    }
}
