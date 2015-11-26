package org.javameta.samples.validate;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
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
