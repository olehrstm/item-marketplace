package de.ole101.marketplace.common.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Locale;
import java.util.function.Supplier;

public class TranslationServiceImpl implements TranslationService {

    private final TranslationProvider provider;
    private final Locale fallbackLocale;
    private final MiniMessage miniMessage;
    private final Interpolator interpolator;
    private final Supplier<Locale> localeSupplier;

    private TranslationServiceImpl(Builder builder) {
        this.provider = builder.provider;
        this.fallbackLocale = builder.fallbackLocale;
        this.miniMessage = builder.miniMessage;
        this.interpolator = builder.interpolator != null ? builder.interpolator : new DefaultInterpolator(this.miniMessage);
        this.localeSupplier = builder.localeSupplier != null ? builder.localeSupplier : () -> this.fallbackLocale;
    }

    @Override
    public String translateRaw(String key, Locale locale, TranslationContext context) {
        String message = getRawTranslation(key, locale);
        return this.interpolator.interpolate(message, context, locale);
    }

    @Override
    public Component translate(String key, Locale locale, TranslationContext context) {
        String interpolated = translateRaw(key, locale, context);
        return this.miniMessage.deserialize(interpolated);
    }

    @Override
    public String getRawTranslation(String key, Locale locale) {
        String message = this.provider.getTranslation(locale, key);
        if (message == null) {
            message = this.provider.getTranslation(this.fallbackLocale, key);
        }
        if (message == null) {
            message = missingTranslationText(key);
        }
        return message;
    }

    @Override
    public Locale resolveLocale() {
        return this.localeSupplier.get();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TranslationProvider provider;
        private Locale fallbackLocale;
        private MiniMessage miniMessage = MiniMessage.builder().build();
        private Interpolator interpolator;
        private Supplier<Locale> localeSupplier;

        public Builder provider(TranslationProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder fallbackLocale(Locale fallbackLocale) {
            this.fallbackLocale = fallbackLocale;
            return this;
        }

        public Builder miniMessage(MiniMessage miniMessage) {
            this.miniMessage = miniMessage;
            return this;
        }

        public Builder interpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public Builder localeSupplier(Supplier<Locale> localeSupplier) {
            this.localeSupplier = localeSupplier;
            return this;
        }

        public TranslationServiceImpl build() {
            if (this.provider == null) {
                throw new IllegalStateException("TranslationProvider must be provided");
            }
            return new TranslationServiceImpl(this);
        }
    }

    protected String missingTranslationText(String key) {
        return "<red>The translation " + key + " could not be found.";
    }
}