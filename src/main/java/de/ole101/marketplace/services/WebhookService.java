package de.ole101.marketplace.services;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.inject.Inject;
import de.ole101.marketplace.common.configurations.Configuration;
import de.ole101.marketplace.common.configurations.WebhookConfiguration;
import de.ole101.marketplace.common.i18n.TranslationContext;
import de.ole101.marketplace.common.i18n.TranslationService;

import java.util.function.Consumer;

public class WebhookService {

    private final WebhookClient webhookClient;
    private final WebhookConfiguration webhookConfiguration;
    private final TranslationService translationService;

    @Inject
    public WebhookService(Configuration configuration, WebhookConfiguration webhookConfiguration, TranslationService translationService) {
        this.webhookClient = WebhookClient.withUrl(configuration.getWebhookUrl());
        this.webhookConfiguration = webhookConfiguration;
        this.translationService = translationService;
    }

    public void sendEmbed(WebhookEmbed embed) {
        this.webhookClient.send(embed);
    }

    public void sendEmbed(String key, TranslationContext context, Consumer<WebhookEmbedBuilder> builder) {
        WebhookConfiguration.Embed embed = this.webhookConfiguration.getEmbeds().get(key);
        if (embed == null) {
            throw new IllegalArgumentException("No embed found for key: " + key);
        }

        String title = this.translationService.translateRaw(embed.getTitleKey(), this.translationService.resolveLocale(), context);
        String description = this.translationService.translateRaw(embed.getDescriptionKey(), this.translationService.resolveLocale(), context);
        Integer color = embed.getColor() != null ? Integer.parseInt(embed.getColor().replace("#", ""), 16) : null;

        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
        builder.accept(embedBuilder);

        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(title, ""));

        if (description != null) {
            embedBuilder.setDescription(description);
        }
        if (color != null) {
            embedBuilder.setColor(color);
        }

        sendEmbed(embedBuilder.build());
    }

    public void sendEmbed(String key, Consumer<TranslationContext> contextConsumer, Consumer<WebhookEmbedBuilder> builder) {
        TranslationContext context = new TranslationContext();
        if (contextConsumer != null) {
            contextConsumer.accept(context);
        }
        sendEmbed(key, context, builder);
    }

    public void sendEmbed(String key, Consumer<WebhookEmbedBuilder> builder) {
        sendEmbed(key, new TranslationContext(), builder);
    }
}
