package de.ole101.marketplace.common.i18n;

import java.util.Locale;

@FunctionalInterface
public interface TranslationProvider {

    String getTranslation(Locale locale, String key);
}
