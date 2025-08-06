package de.ole101.marketplace.listeners;

import com.google.inject.Inject;
import de.ole101.marketplace.services.PlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final PlayerService playerService;

    @Inject
    public QuitListener(PlayerService playerService) {
        this.playerService = playerService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerService.disposePlayer(event.getPlayer());
    }
}