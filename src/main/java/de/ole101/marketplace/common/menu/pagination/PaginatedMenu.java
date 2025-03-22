package de.ole101.marketplace.common.menu.pagination;

import de.ole101.marketplace.common.menu.Menu;
import de.ole101.marketplace.common.menu.item.MenuItem;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.stream.StreamSupport;

public abstract class PaginatedMenu<E> extends Menu {

    protected PaginatedMenuContext<E> context;
    protected int page;

    public abstract PaginatedMenuContext<E> getMenu(Player player);

    @Override
    public Inventory build(Player player, boolean update) {
        return build(player, update, 0);
    }

    @Override
    public void update() {
        if (this.inventory == null) {
            return;
        }

        this.inventory.clear();

        List<HumanEntity> viewers = List.copyOf(this.inventory.getViewers());
        viewers.forEach(viewer -> build((Player) viewer, true, this.page));
    }

    public Inventory build(Player player, boolean update, int page) {
        this.context = getMenu(player);
        this.page = page;

        this.context.getMenuItems().removeIf(menuItem -> menuItem.getSlot() == -1); // remove pagination items
        updateMenuContext(this.context);
        super.build(player, update);

        int entriesPerPage = getEntriesPerPage();
        int start = this.page * entriesPerPage;
        StreamSupport.stream(this.context.getIterable().spliterator(), false)
                .skip(start)
                .limit(entriesPerPage)
                .forEach(element -> {
                    MenuItem menuItem = this.context.getItemProvider().apply(element);

                    updateItem(menuItem);
                });

        return this.inventory;
    }

    @Override
    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        player.openInventory(build(player, false, page));
    }

    public int getTotalPages() {
        int count = (int) this.context.getIterable().spliterator().getExactSizeIfKnown();
        int entriesPerPage = getEntriesPerPage();
        double pages = (double) count / entriesPerPage;
        return (int) Math.ceil(pages);
    }

    public int getEntriesPerPage() {
        return this.context.getRows() * 9 - (int) this.context.getMenuItems().stream().map(MenuItem::getSlot).distinct().count();
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public boolean hasNextPage() {
        return this.page < getTotalPages() - 1;
    }

    public void nextPage() {
        if (hasNextPage()) {
            this.page++;
            update();
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            this.page--;
            update();
        }
    }

    public void gotoPage(int page) {
        if (page >= 0 && page < getTotalPages()) {
            this.page = page;
            update();
        }
    }
}
