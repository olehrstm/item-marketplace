package de.ole101.marketplace.common.menu.pagination;

import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.i18n.TranslationContext;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.menu.MenuContext;
import de.ole101.marketplace.common.menu.item.Click;
import de.ole101.marketplace.common.menu.item.MenuItem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

@Getter
@Setter
public class PaginatedMenuContext<E> extends MenuContext {

    private Iterable<E> iterable;
    private Function<E, MenuItem> itemProvider;
    private Consumer<Player> closeConsumer;

    public PaginatedMenuContext(Component title, int rows, Set<MenuItem> menuItems, MenuItem fillItem, Iterable<E> iterable, Function<E, MenuItem> itemProvider, Consumer<Player> closeConsumer) {
        super(title, rows, menuItems, fillItem, closeConsumer);
        this.iterable = iterable;
        this.itemProvider = itemProvider;
        this.closeConsumer = closeConsumer;
    }

    public static <T> Builder<T> paginated() {
        return new Builder<>();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder<E> {

        private static TranslationService translationService;
        private final Set<MenuItem> menuItems = new HashSet<>();
        private Component title = text("Menu");
        private int rows = 3;
        private MenuItem fillItem;
        private Iterable<E> iterable;
        private Function<E, MenuItem> itemProvider;
        private Consumer<Player> closeConsumer;

        public Builder<E> translated(String key) {
            return translated(key, context -> {});
        }

        public Builder<E> translated(String key, Consumer<TranslationContext> consumer) {
            return title(getTranslationService().translate(key, consumer));
        }

        public Builder<E> item(MenuItem menuItem) {
            return item(menuItem.getSlot(), menuItem.getItemStack(), menuItem.getFunction());
        }

        public Builder<E> item(int slot, ItemStack itemStack) {
            return item(slot, itemStack, null);
        }

        public Builder<E> item(int row, int column, ItemStack itemStack) {
            return item(row, column, itemStack, null);
        }

        public Builder<E> item(int row, int column, ItemStack itemStack, Consumer<Click> function) {
            return item(row * 9 - 9 + (column - 1), itemStack, function);
        }

        public Builder<E> item(int slot, ItemStack itemStack, Consumer<Click> function) {
            this.menuItems.add(MenuItem.builder()
                    .slot(slot)
                    .itemStack(itemStack)
                    .function(function)
                    .build());
            return this;
        }

        public PaginatedMenuContext<E> build() {
            return new PaginatedMenuContext<>(this.title, this.rows, this.menuItems, this.fillItem, this.iterable, this.itemProvider, this.closeConsumer);
        }

        private static TranslationService getTranslationService() {
            if (translationService == null) {
                translationService = MarketplacePlugin.getPlugin().getInjector().getInstance(TranslationService.class);
            }
            return translationService;
        }
    }
}
