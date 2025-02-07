package talium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Automatically calculates a progressbar to be displayed to the user.
 * Caller needs to print it out himself.
 */
public class ASCIIProgressbar {

    private static final Logger logger = LoggerFactory.getLogger(ASCIIProgressbar.class);
    private static final boolean isUTF8 = new OutputStreamWriter(System.out).getEncoding().equals("UTF8");
    //TOTO Change to builder Pattern and add Units to the Amounts
    public static void display() {
        String pos = "#■|◆";
        String neg = "-.◦ ";
        ArrayList<String> strings = new ArrayList<>();
//        strings.add("[####------]");
//        strings.add("[####◦◦◦◦◦◦]");
//        strings.add("[####......]");
//        strings.add("[####      ]");
//        strings.add("[■■■■------]");
//        strings.add("[■■■■◦◦◦◦◦◦]");
//        strings.add("[■■■■......]");
//        strings.add("[■■■■      ]");
//        strings.add("[||||------]");
//        strings.add("[||||......]");
//        strings.add("[||||◦◦◦◦◦◦]");
//        strings.add("[||||      ]");
//        strings.add("[◆◆◆◆------]");
//        strings.add("[◆◆◆◆◦◦◦◦◦◦]");
//        strings.add("[◆◆◆◆......]");
//        strings.add("[◆◆◆◆      ]");
        strings.add("[▨▨▨▨▢▢▢▢▢▢]");
        strings.forEach(logger::info);

        for (char aChar : pos.toCharArray()) {
            String p = String.valueOf(aChar);
            for (char cChar : neg.toCharArray()) {
                String c = String.valueOf(cChar);
                logger.info("[{}{}]", p.repeat(4), c.repeat(6));
            }
        }
    }

    public static String consoleBar(double value, double maxValue) {
        if (isUTF8)
            return bar(value, maxValue, 10, "■", ".", true, true);
        return bar(value, maxValue, 10, "#", " ", true, true);
    }

    public static String bar(double value, double maxValue, int length) {
        return bar(value, maxValue, length, "■", ".", true, true);
    }

    public static String bar(double value, double maxValue, int length, String symbols) {
        if (symbols.length() < 2)
            return bar(value, maxValue, length, "■", ".", true, true);
        return bar(value, maxValue, length, symbols.substring(0, 1), symbols.substring(1, 2), true, true);
    }

    public static String bar(double value, double maxValue, int length, String completed, String placeHolder, boolean withPercent, boolean withAmount) {
        double percent;
        if (maxValue != 0)
            percent = value / maxValue;
        else
            percent = 1.D;

        double amountPercent = percent;
        if (amountPercent > 1.D)
            amountPercent = 1.D;
        if (amountPercent < 0)
            amountPercent = 0;

        int amountPos = (int) Math.round(length * amountPercent);
        int amountRest = length - amountPos;
        String output = "[" + completed.repeat(amountPos) + placeHolder.repeat(amountRest) + "] ";

        double rounded2D = (double) Math.round(percent * 10000.D) / 100.D;

        if (withPercent)
            output += format(rounded2D) + "% ";
        if (withAmount)
            output += "[" + format(value) + "/" + format(maxValue) + "]";
        return output;
    }

    private static String format(double number) {
        return String.format(Locale.US,"%.2f", number);
    }
}
