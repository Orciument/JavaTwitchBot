package talium.inputs.TipeeeStream;

import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Optional;

public record DonationEvent(
        long donationId,
        String donationService,
        String serviceDonationId,
        Currency currency,
        long amount_cents,
        Optional<String> message,
        String userName,
        ZonedDateTime donated_at
) {
    public DonationEvent(DonationEntity entity) {
        this(entity.donationId, entity.donationService, entity.serviceDonationId, Currency.getInstance(entity.currency_code), entity.amount_cents, Optional.ofNullable(entity.message), entity.userName, entity.donated_at);
    }
}