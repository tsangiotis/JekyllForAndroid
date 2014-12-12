package gr.tsagi.jekyllforandroid.app.util;

import java.util.Locale;

/**
 * Created by tsagi on 12/12/14.
 */
public class HashUtils {
    public static String computeWeakHash(String string) {
        return String.format(Locale.US, "%08x%08x", string.hashCode(), string.length());
    }
}
