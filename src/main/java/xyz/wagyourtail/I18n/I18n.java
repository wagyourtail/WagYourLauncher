package xyz.wagyourtail.I18n;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;

public class I18n {

    protected static Locale currentLocale = Locale.getDefault();

    protected static JsonObject keys;
    protected static JsonObject fallback;

    static {
        try {
            setLocale(currentLocale);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(I18n.class.getResourceAsStream("/lang/en_us.json")))) {
             fallback = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setLocale(Locale locale) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(I18n.class.getResourceAsStream(
            "/lang/" +
            locale.toLanguageTag().replace("-", "_").toLowerCase(Locale.ROOT) +
            ".json")))
        ) {
            keys = JsonParser.parseReader(reader).getAsJsonObject();
            currentLocale = locale;
        } catch (NullPointerException e) {
            throw new IOException("Language file not found for locale " + locale.toLanguageTag());
        }
    }

    public static String get(String key) {
        return key;
    }

}
