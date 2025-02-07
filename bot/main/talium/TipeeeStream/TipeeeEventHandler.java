package talium.TipeeeStream;

import talium.eventSystem.EventDispatcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Currency;

public class TipeeeEventHandler {

    static Logger LOGGER = LoggerFactory.getLogger(TipeeeEventHandler.class);

    public static void handleDonationEvent(String eventString, DonationRepo repo) {
        DonationEntity donationEntity;
        try {
            JSONObject event = new JSONArray(eventString)
                    .getJSONObject(0)
                    .getJSONObject("event");

            String eventType = event.getString("type");
            if (!eventType.equals("donation")) {
                LOGGER.debug("Event ignored, not a DonationEvent");
                return;
            }

            LOGGER.debug("Collected DonationEvent: {}", eventString);
            donationEntity = parseToInstance(event);
            LOGGER.debug("Parsed DonationEntity: {}-{}", donationEntity.donationService, donationEntity.serviceDonationId);
        } catch (JSONException e) {
            LOGGER.error("Error parsing tipeee event: \n" + eventString, e);
            return;
        }

        try {
            donationEntity = repo.save(donationEntity);
        } catch (Exception e) {
            LOGGER.error("Could not save DonationEntity" , e);
            return;
        }
        LOGGER.debug("Saved DonationEvent: {}-{}", donationEntity.donationService, donationEntity.serviceDonationId);

        EventDispatcher.dispatch(new DonationEvent(donationEntity));
    }

    private static DonationEntity parseToInstance(JSONObject json) throws JSONException {
        JSONObject para = json.getJSONObject("parameters");

        String message = null;
        if (para.has("message")) {
            message = para.getString("message");
        }

        String createdAt = json.getString("created_at");
        ZonedDateTime donated_at = ZonedDateTime.parse(createdAt.substring(0, createdAt.length() - 2));

        String donationId = json.getString("id");
        long amount_cents = (long) (para.getDouble("amount") * 100);
        String currency_code = json.getJSONObject("user").getJSONObject("currency").getString("code");
        Currency currency = Currency.getInstance(currency_code);

        String username = para.getString("username");
        return new DonationEntity("twitch", donationId, currency, amount_cents, message, username, donated_at);
    }

}
