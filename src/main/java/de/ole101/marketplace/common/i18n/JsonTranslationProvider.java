package de.ole101.marketplace.common.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JsonTranslationProvider implements TranslationProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LANG_DIRECTORY = "plugins/ItemMarketplace/lang";
    private final String[] baseNames;
    private final Map<Locale, Properties> cache = new ConcurrentHashMap<>();
    private final Locale fallbackLocale;

    public JsonTranslationProvider(Locale fallbackLocale, String... baseNames) {
        this.baseNames = baseNames;
        this.fallbackLocale = fallbackLocale;
    }

    @Override
    public String getTranslation(Locale locale, String key) {
        // Try an exact locale match
        String translation = getTranslationFromLocale(locale, key);

        if (translation == null && !locale.equals(this.fallbackLocale)) {
            // Try language-only match (e.g., "en_US" -> "en")
            Locale languageOnlyLocale = Locale.of(locale.getLanguage());
            if (!languageOnlyLocale.equals(locale)) {
                translation = getTranslationFromLocale(languageOnlyLocale, key);
            }
            // Try fallback locale as a last resort
            if (translation == null) {
                translation = getTranslationFromLocale(this.fallbackLocale, key);
            }
            // If still not found, log a warning
            if (translation == null) {
                log.warn("Translation not found for key: {} in locale: {}. Falling back to default locale: {}", key,
                        locale, this.fallbackLocale);
                translation = "Translation not found for key: " + key;
            }
        }

        return translation;
    }

    /**
     * Clears the cache for a specific locale.
     */
    public void clearCache(Locale locale) {
        this.cache.remove(locale);
    }

    /**
     * Clears the entire cache.
     */
    public void clearCache() {
        this.cache.clear();
    }

    private String getTranslationFromLocale(Locale locale, String key) {
        Properties props = getPropertiesForLocale(locale);
        return props.getProperty(key);
    }

    private Properties getPropertiesForLocale(Locale locale) {
        return this.cache.computeIfAbsent(locale, this::loadProperties);
    }

    private Properties loadProperties(Locale locale) {
        Properties props = new Properties();
        for (String baseName : this.baseNames) {
            // Build the file name e.g. "messages_en_US.json"
            String fileName = baseName + "_" + locale.toString() + ".json";
            File file = new File(LANG_DIRECTORY, fileName);
            if (!file.exists()) {
                log.warn("JSON translation file not found: {} for locale: {}", file.getAbsolutePath(), locale);
                continue;
            }
            try {
                // Read JSON file into a Map<String, String>
                Map<String, String> translations = OBJECT_MAPPER.readValue(file, Map.class);
                // Add keys if they are not already present
                translations.forEach(props::putIfAbsent);
            } catch (IOException e) {
                log.error("Failed to load JSON translation file: {} for locale: {}", file.getAbsolutePath(), locale, e);
            }
        }
        return props;
    }
}

