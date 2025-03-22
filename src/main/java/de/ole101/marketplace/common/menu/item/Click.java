package de.ole101.marketplace.common.menu.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

@Data
@AllArgsConstructor
public class Click {

    private Player player;
    private ClickType clickType;
    private InventoryClickEvent event;
}
