package de.ole101.marketplace.common;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.configurations.Configuration;
import de.ole101.marketplace.common.wrapper.MongoWrapper;
import de.ole101.marketplace.repositories.UserRepository;
import de.ole101.marketplace.repositories.impl.UserRepositoryImpl;
import de.ole101.marketplace.services.ConfigService;
import de.ole101.marketplace.services.MarketplaceService;
import de.ole101.marketplace.services.PlayerService;
import de.ole101.marketplace.services.UserService;
import de.ole101.marketplace.services.impl.UserServiceImpl;

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

        MongoWrapper mongoWrapper = new MongoWrapper(config);
        bind(MongoWrapper.class).toInstance(mongoWrapper);
        bind(MongoClient.class).toInstance(mongoWrapper.mongoClient());
        bind(MongoDatabase.class).toInstance(mongoWrapper.mongoDatabase());

        bind(UserService.class).to(UserServiceImpl.class).asEagerSingleton();
        bind(UserRepository.class).to(UserRepositoryImpl.class).asEagerSingleton();

        bind(Registry.class).asEagerSingleton();
        bind(PlayerService.class).asEagerSingleton();
        bind(MarketplaceService.class).asEagerSingleton();
    }
}
