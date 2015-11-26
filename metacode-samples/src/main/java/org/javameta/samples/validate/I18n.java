package org.javameta.samples.validate;

/**
 * @author khalidov
 * @version $Id$
 */
public class I18n {

    public static String get(String key, String lang) {
        switch (lang) {
            case "en":
                switch (key) {
                    case "TOO_YOUNG":
                        return "Too young";
                    default:
                        return null;
                }
            default:
                return null;
        }
    }
}
