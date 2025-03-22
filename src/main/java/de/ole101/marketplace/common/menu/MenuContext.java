package de.ole101.marketplace.common.menu;

import de.ole101.marketplace.common.menu.item.Click;
import de.ole101.marketplace.common.menu.item.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

@Data
@AllArgsConstructor
public class MenuContext {

    private Component title;
    private int rows;
    private Set<MenuItem> menuItems;
    private MenuItem fillItem;
    private Consumer<Player> closeConsumer;

    public static Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {

        private final Set<MenuItem> menuItems = new HashSet<>();
        private Component title = text("Menu");
        private int rows = 3;
        private MenuItem fillItem;
        private Consumer<Player> closeConsumer;

        public Builder item(MenuItem menuItem) {
            this.menuItems.add(menuItem);
            return this;
        }

        public Builder item(int slot, ItemStack itemStack) {
            return item(slot, itemStack, null);
        }

        public Builder item(int row, int column, ItemStack itemStack) {
            return item(row, column, itemStack, null);
        }

        public Builder item(int row, int column, ItemStack itemStack, Consumer<Click> function) {
            return item(row * 9 - 9 + (column - 1), itemStack, function);
        }

        public Builder item(int slot, ItemStack itemStack, Consumer<Click> function) {
            return item(MenuItem.builder()
                    .slot(slot)
                    .itemStack(itemStack)
                    .function(function)
                    .build());
        }

        public MenuContext build() {
            return new MenuContext(this.title, this.rows, this.menuItems, this.fillItem, this.closeConsumer);
        }
    }
}
