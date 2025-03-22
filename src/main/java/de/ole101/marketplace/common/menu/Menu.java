package de.ole101.marketplace.common.menu;

import de.ole101.marketplace.common.items.ItemBuilder;
import de.ole101.marketplace.common.menu.item.Click;
import de.ole101.marketplace.common.menu.item.MenuItem;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenuContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class Menu implements IMenu {

    protected Inventory inventory;
    protected MenuContext context;
    private static final NamespacedKey UUID_KEY = new NamespacedKey("marketplace", "uuid");

    public abstract MenuContext getMenu(Player player);

    @Override
    public Inventory build(Player player, boolean update) {
        if (!update && !(this.context instanceof PaginatedMenuContext<?>)) {
            this.context = getMenu(player);
        }

        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, this.context.getRows() * 9, this.context.getTitle());
        }

        this.context.getMenuItems().add(ItemBuilder.FILL_MENU_ITEM);

        String[] layout = this.context.getLayout().split("\\n");
        this.context.getMenuItems().forEach(menuItem -> {
            for (int row = 0; row < layout.length; row++) {
                for (int col = 0; col < layout[row].length(); col++) {
                    // Check if the character at this position matches the ingredient key
                    if (String.valueOf(layout[row].charAt(col)).equals(menuItem.getLayoutId())) {
                        // Calculate the slot number in a row-major order
                        int slot = row * layout[row].length() + col;
                        MenuItem copy = MenuItem.builder()
                                .slot(slot)
                                .itemStack(menuItem.getItemStack())
                                .function(menuItem.getFunction())
                                .build();
                        this.context.getMenuItems().add(copy);
                        updateItem(copy);
                    }
                }
            }
        });

        return this.inventory;
    }

    @Override
    public void onClick(Player player, ClickType clickType, ItemStack clickedItem, InventoryClickEvent event) {
        this.context.getMenuItems().stream()
                .filter(menuItem -> menuItem.getUniqueId().toString().equals(clickedItem.getPersistentDataContainer().get(UUID_KEY, PersistentDataType.STRING)))
                .findFirst()
                .filter(menuItem -> menuItem.getFunction() != null)
                .ifPresent(menuItem -> menuItem.getFunction().accept(new Click(player, clickType, event)));
    }

    @Override
    public void update() {
        if (this.inventory == null) {
            return;
        }

        this.inventory.clear();

        List<HumanEntity> viewers = List.copyOf(this.inventory.getViewers());
        viewers.forEach(viewer -> build((Player) viewer, true));
    }

    @Override
    public void updateItem(MenuItem menuItem) {
        UUID uniqueId = UUID.randomUUID();
        menuItem.setUniqueId(uniqueId);

        ItemStack itemStack = menuItem.getItemStack();
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, menuItem.getUniqueId().toString()));
        this.inventory.setItem(menuItem.getSlot(), itemStack);

        this.context.getMenuItems().add(menuItem);
    }

    @Override
    public void updateMenuContext(MenuContext menuContext) {
        this.context = menuContext;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
