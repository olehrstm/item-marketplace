package de.ole101.marketplace;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ole101.marketplace.common.GuiceModule;
import de.ole101.marketplace.common.Registry;
import de.ole101.marketplace.common.wrapper.MongoWrapper;
import de.ole101.marketplace.listeners.InventoryListener;
import de.ole101.marketplace.listeners.JoinListener;
import de.ole101.marketplace.listeners.QuitListener;
import de.ole101.marketplace.services.MarketplaceService;
import de.ole101.marketplace.services.PlayerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

@Slf4j
@Getter
public class MarketplacePlugin extends JavaPlugin {

    public static MiniMessage MM;
    private static final String[] RESOURCE_PATHS = {
            "config.json",
            "menus.json",
            "webhooks.json",
            "lang/common_en.json",
            "lang/command_en.json",
            "lang/menu_en.json",
            "lang/webhook_en.json"
    };
    @Getter
    private static MarketplacePlugin plugin;
    private final Injector injector;
    private final PlayerService playerService;
    private final MarketplaceService marketplaceService;

    public MarketplacePlugin() {
        plugin = this;
        MM = MiniMessage.miniMessage();

        for (String resourcePath : RESOURCE_PATHS) {
            if (!Path.of(resourcePath).toFile().exists()) {
                continue;
            }

            saveResource(resourcePath, false);
        }

        this.injector = Guice.createInjector(new GuiceModule(this));
        this.playerService = this.injector.getInstance(PlayerService.class);
        this.marketplaceService = this.injector.getInstance(MarketplaceService.class);
    }

    @Override
    public void onLoad() {
        this.playerService.loadAllUsers();
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(this.playerService::disposePlayer);
        this.injector.getInstance(MongoWrapper.class).close();
        log.info("Disabled ItemMarketplace!");
    }

    @Override
    public void onEnable() {
        this.injector.getInstance(Registry.class).registerAllCommands(getLifecycleManager());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this.injector.getInstance(JoinListener.class), this);
        pluginManager.registerEvents(this.injector.getInstance(QuitListener.class), this);
        pluginManager.registerEvents(this.injector.getInstance(InventoryListener.class), this);

        this.marketplaceService.scheduleBlackMarketRefresh();

        log.info("Enabled ItemMarketplace!");
    }
}
