package de.ole101.marketplace.common.i18n;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.function.Consumer;

public interface TranslationService {

    /**
     * Translates a key with the given context and locale
     */
    Component translate(String key, Locale locale, TranslationContext context);

    /**
     * Translates a key with a provided context builder and resolves locale automatically
     */
    default Component translate(String key, Consumer<TranslationContext> contextConsumer) {
        TranslationContext context = new TranslationContext();
        if (contextConsumer != null) {
            contextConsumer.accept(context);
        }
        return translate(key, resolveLocale(), context);
    }

    /**
     * Translates a key with an empty context and resolves locale automatically
     */
    default Component translate(String key) {
        return translate(key, resolveLocale(), new TranslationContext());
    }

    /**
     * Translates a key with the given context and resolves locale automatically
     */
    default Component translate(String key, TranslationContext context) {
        return translate(key, resolveLocale(), context);
    }

    /**
     * Translates a key with the given locale and empty context
     */
    default Component translate(String key, Locale locale) {
        return translate(key, locale, new TranslationContext());
    }

    /**
     * Sends a translated message to the specified player with the given context builder
     */
    default void send(Player player, String key, Consumer<TranslationContext> contextConsumer) {
        TranslationContext context = new TranslationContext();
        if (contextConsumer != null) {
            contextConsumer.accept(context);
        }
        send(player, key, context);
    }

    /**
     * Sends a translated message to the specified player with an empty context
     */
    default void send(Player player, String key) {
        send(player, key, new TranslationContext());
    }

    /**
     * Sends a translated message to the specified player with the given context
     */
    default void send(Player player, String key, TranslationContext context) {
        player.sendMessage(translate(key, resolveLocale(), context));
    }

    /**
     * Gets the raw translation string without interpolation or MiniMessage processing
     */
    String getRawTranslation(String key, Locale locale);

    /**
     * Resolves the current locale
     */
    Locale resolveLocale();
}