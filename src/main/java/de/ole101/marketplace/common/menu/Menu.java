package de.ole101.marketplace.common.menu;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class Menu implements IMenu {

    protected Inventory inventory;
    protected MenuContext context;
    private static final NamespacedKey UUID_KEY = new NamespacedKey("novacity", "uuid");

    public abstract MenuContext getMenu(Player player);

    @Override
    public Inventory build(Player player, boolean update) {
        if (!update && !(this.context instanceof PaginatedMenuContext<?>)) {
            this.context = getMenu(player);
        }

        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, this.context.getRows() * 9, this.context.getTitle());
        }

        Set<MenuItem> menuItems = new HashSet<>(this.context.getMenuItems());
        MenuItem fillItem = this.context.getFillItem();
        if (fillItem != null) {
            fillBorder(fillItem);
        }

        menuItems.forEach(menuItem -> {
            ItemStack itemStack = menuItem.getItemStack();
            itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, menuItem.getUniqueId().toString()));
            updateItem(menuItem);
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
        int slot = menuItem.getSlot();
        if (menuItem.getSlot() == -1) {
            int firstEmpty = this.inventory.firstEmpty();

            if (firstEmpty == -1) {
                return;
            }
            slot = firstEmpty;
        }
        this.inventory.setItem(slot, itemStack);

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

    private void fillBorder(MenuItem fillItem) {
        for (int i = 0; i < this.inventory.getSize(); i++) {
            int numCols = 9;
            if (this.inventory.getSize() <= 9) {
                numCols = (int) Math.sqrt(this.inventory.getSize());
            }

            boolean isFirstRow = i < numCols;
            boolean isLastRow = i >= this.inventory.getSize() - numCols;
            boolean isFirstColumn = i % numCols == 0;
            boolean isLastColumn = i % numCols == (numCols - 1);
            if (isFirstRow || isLastRow || isFirstColumn || isLastColumn) {
                MenuItem menuItem = MenuItem.builder()
                        .itemStack(fillItem.getItemStack())
                        .slot(i)
                        .uniqueId(fillItem.getUniqueId())
                        .function(fillItem.getFunction())
                        .build();
                updateItem(menuItem);
            }
        }
    }
}
