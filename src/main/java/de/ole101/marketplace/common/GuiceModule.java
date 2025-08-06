package de.ole101.marketplace.common;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.configurations.Configuration;
import de.ole101.marketplace.common.configurations.MenuConfiguration;
import de.ole101.marketplace.common.configurations.WebhookConfiguration;
import de.ole101.marketplace.common.i18n.JsonTranslationProvider;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.i18n.TranslationServiceImpl;
import de.ole101.marketplace.common.wrapper.MongoWrapper;
import de.ole101.marketplace.repositories.UserRepository;
import de.ole101.marketplace.repositories.impl.UserRepositoryImpl;
import de.ole101.marketplace.services.ConfigService;
import de.ole101.marketplace.services.MarketplaceService;
import de.ole101.marketplace.services.MenuService;
import de.ole101.marketplace.services.PlayerService;
import de.ole101.marketplace.services.UserService;
import de.ole101.marketplace.services.WebhookService;
import de.ole101.marketplace.services.impl.UserServiceImpl;

import static de.ole101.marketplace.MarketplacePlugin.MM;

public class GuiceModule extends AbstractModule {

    private final MarketplacePlugin plugin;

    public GuiceModule(MarketplacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(MarketplacePlugin.class).toInstance(this.plugin);

        ConfigService configService = new ConfigService();
        bind(ConfigService.class).toInstance(configService);
        Configuration config = configService.loadConfig(Configuration.class, "config.json");
        bind(Configuration.class).toInstance(config);
        MenuConfiguration menuConfig = configService.loadConfig(MenuConfiguration.class, "menus.json");
        bind(MenuConfiguration.class).toInstance(menuConfig);
        WebhookConfiguration webhookConfiguration = configService.loadConfig(WebhookConfiguration.class,
                "webhooks.json");
        bind(WebhookConfiguration.class).toInstance(webhookConfiguration);

        MongoWrapper mongoWrapper = new MongoWrapper(config);
        bind(MongoWrapper.class).toInstance(mongoWrapper);
        bind(MongoClient.class).toInstance(mongoWrapper.mongoClient());
        bind(MongoDatabase.class).toInstance(mongoWrapper.mongoDatabase());

        bind(UserService.class).to(UserServiceImpl.class).asEagerSingleton();
        bind(UserRepository.class).to(UserRepositoryImpl.class).asEagerSingleton();

        bind(Registry.class).asEagerSingleton();
        bind(PlayerService.class).asEagerSingleton();
        bind(MarketplaceService.class).asEagerSingleton();
        bind(MenuService.class).asEagerSingleton();

        bind(TranslationService.class).toInstance(TranslationServiceImpl.builder()
                .provider(new JsonTranslationProvider(
                        config.getFallbackLocale(), "common", "command", "menu", "webhook"))
                .fallbackLocale(config.getFallbackLocale())
                .localeSupplier(config::getLocale)
                .miniMessage(MM)
                .build());

        bind(WebhookService.class).asEagerSingleton();
    }
}