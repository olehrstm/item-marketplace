package de.ole101.marketplace;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ole101.marketplace.common.GuiceModule;
import de.ole101.marketplace.common.Registry;
import de.ole101.marketplace.common.wrapper.MongoWrapper;
import de.ole101.marketplace.listeners.InventoryListener;
import de.ole101.marketplace.listeners.JoinListener;
import de.ole101.marketplace.listeners.QuitListener;
import de.ole101.marketplace.services.PlayerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Slf4j
@Getter
public class MarketplacePlugin extends JavaPlugin {

    public static MiniMessage MM;
    @Getter
    private static MarketplacePlugin plugin;
    private final Injector injector;
    private final PlayerService playerService;

    public MarketplacePlugin() {
        plugin = this;
        MM = MiniMessage.miniMessage();

        if (!new File(getDataFolder(), "config.json").exists()) {
            saveResource("config.json", false);
        }
        if (!new File(getDataFolder(), "menus.json").exists() || true) {
            saveResource("menus.json", true);
        }
        new File(getDataFolder(), "webhooks.json").exists();
        if (!new File(getDataFolder(), "webhooks.json").exists() || true) {
            saveResource("webhooks.json", true);
        }
        if (!new File(getDataFolder(), "lang").exists() || true) { // TODO: dont forget to remove this (only for dev purposes)
            saveResource("lang/common_en.json", true);
            saveResource("lang/command_en.json", true);
            saveResource("lang/menu_en.json", true);
            saveResource("lang/webhook_en.json", true);
        } // TODO: migrate to new economy, black market

        this.injector = Guice.createInjector(new GuiceModule(this));
        this.playerService = this.injector.getInstance(PlayerService.class);
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

        log.info("Enabled ItemMarketplace!");
    }
}
