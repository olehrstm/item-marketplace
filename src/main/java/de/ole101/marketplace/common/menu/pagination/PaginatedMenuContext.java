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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

@Getter
@Setter
public class PaginatedMenuContext<E> extends MenuContext {

    private Iterable<E> iterable;
    private Function<E, MenuItem> itemProvider;
    private String itemId;

    public PaginatedMenuContext(Component title, Set<MenuItem> menuItems, Consumer<Player> closeConsumer, String layout, Iterable<E> iterable, Function<E, MenuItem> itemProvider, String itemId) {
        super(title, menuItems, closeConsumer, layout);
        this.iterable = iterable;
        this.itemProvider = itemProvider;
        this.itemId = itemId;
    }

    public static <T> Builder<T> paginated() {
        return new Builder<>();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder<E> {

        private static TranslationService translationService;
        private final Set<MenuItem> menuItems = ConcurrentHashMap.newKeySet();
        private Component title = text("Menu");
        private Iterable<E> iterable;
        private Function<E, MenuItem> itemProvider;
        private Consumer<Player> closeConsumer;
        private String layout = """
                                #########
                                #########
                                #########
                                """;
        private String itemId;

        public Builder<E> translated(String key) {
            return translated(key, context -> {});
        }

        public Builder<E> translated(String key, Consumer<TranslationContext> consumer) {
            return title(getTranslationService().translate(key, consumer));
        }

        public Builder<E> item(MenuItem menuItem) {
            this.menuItems.add(menuItem);
            return this;
        }

        public Builder<E> item(ItemStack itemStack, String layoutId) {
            return item(itemStack, layoutId, null);
        }

        public Builder<E> item(ItemStack itemStack, String layoutId, Consumer<Click> function) {
            return item(MenuItem.builder()
                    .itemStack(itemStack)
                    .function(function)
                    .layoutId(layoutId)
                    .build());
        }

        public PaginatedMenuContext<E> build() {
            return new PaginatedMenuContext<>(this.title, this.menuItems, this.closeConsumer, this.layout, this.iterable, this.itemProvider, this.itemId);
        }

        private static TranslationService getTranslationService() {
            if (translationService == null) {
                translationService = MarketplacePlugin.getPlugin().getInjector().getInstance(TranslationService.class);
            }
            return translationService;
        }
    }
}
