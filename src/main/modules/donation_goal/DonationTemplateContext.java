package main.modules.donation_goal;

import main.inputs.TipeeeStream.DonationEvent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

public class DonationTemplateContext {
    private static final DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter timeFormater = DateTimeFormatter.ofPattern("HH:mm");

    public final String currencyCode;
    public final String currencySymbol;
    public final double amount;
    public final String message;
    public final boolean hasMessage;
    public final String tipeee_username;
    public final String time;
    public final String date;


    public DonationTemplateContext(DonationEvent event) {
        this.currencyCode = "EUR";
        this.currencySymbol = "€";
        this.amount = event.amount;
        this.message = event.message;
        this.hasMessage = event.hasMessage;
        this.tipeee_username = event.tipeee_username;
        this.date = event.donated_at.format(dateFormater);
        this.time = event.donated_at.format(timeFormater);
    }
}

