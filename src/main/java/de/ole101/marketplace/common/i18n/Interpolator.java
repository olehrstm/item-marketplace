package de.ole101.marketplace.common.i18n;

import java.util.Locale;

@FunctionalInterface
public interface Interpolator {

    String interpolate(String message, TranslationContext context, Locale locale);
}

