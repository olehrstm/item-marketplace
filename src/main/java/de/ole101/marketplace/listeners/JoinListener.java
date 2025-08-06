package de.ole101.marketplace.listeners;

import com.google.inject.Inject;
import de.ole101.marketplace.services.PlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final PlayerService playerService;

    @Inject
    public JoinListener(PlayerService playerService) {
        this.playerService = playerService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.playerService.registerPlayer(event.getPlayer());
    }
}
