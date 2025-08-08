package de.ole101.marketplace.common.configurations;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Data
@Slf4j
public class Configuration {

    private String mongoUri;
    private String mongoDatabaseName;
    private String webhookUrl;
    private Locale locale;
    private Locale fallbackLocale;
    private long maxBlackMarketItems;
    private long blackMarketRefreshInterval;
}

