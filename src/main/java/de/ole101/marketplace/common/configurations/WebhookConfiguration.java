package de.ole101.marketplace.common.configurations;

import lombok.Data;

import java.util.Map;

@Data
public class WebhookConfiguration {

    private Map<String, Embed> embeds;

    @Data
    public static class Embed {

        private String titleKey;
        private String descriptionKey;
        private String color;
    }
}
