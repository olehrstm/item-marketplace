package de.ole101.marketplace.listeners;

import de.ole101.marketplace.common.menu.Menu;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

@Slf4j
public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof Menu menu)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        menu.onClick((Player) event.getWhoClicked(), event.getClick(), event.getCurrentItem(), event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof Menu menu)) {
            return;
        }

        Consumer<Player> closeConsumer = menu.getContext().getCloseConsumer();
        if (closeConsumer == null) {
            return;
        }

        closeConsumer.accept((Player) event.getPlayer());
    }
}