package de.ole101.marketplace.common.menu;

import de.ole101.marketplace.common.menu.item.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface IMenu extends InventoryHolder {

    Inventory build(Player player, boolean update);

    void onClick(Player player, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event);

    void update();

    void updateItem(MenuItem menuItem);

    void updateMenuContext(MenuContext menuContext);

    default void open(Player player) {
        player.openInventory(build(player, false));
    }

    default void close(Player player) {
        player.closeInventory();
    }
}

