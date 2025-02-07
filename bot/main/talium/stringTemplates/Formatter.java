package talium.stringTemplates;

/**
 * Provides basic Formatting functions, manly to be used in Template Contexts
 */
public class Formatter {

    /**
     * Formats the double with 2 decimal places except when the 2 first decimal places would be 00, then zero decimal places are returned
     * @param d Double to be formated
     * @return formated String
     */
    public static String formatDouble(double d) {
        String s = String.format("%.2f", d);
        return s.endsWith(".00") ? s.substring(0, s.length() - 3) : s;
    }

    /**
     * Formats the double with 2 decimal places except when the 2 first decimal places would be 00, then zero decimal places are returned. <br/>
     * Uses the Comma , as the separator for the decimal places
     * @param d Double to be formated
     * @return formated String
     */
    public static String formatDoubleComma(double d) {
        String s = String.format("%.2f", d).replace('.', ',');
        return s.endsWith(",00") ? s.substring(0, s.length() - 3) : s;
    }
}
