package de.ole101.marketplace.common.menu;

import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.i18n.TranslationContext;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.menu.item.Click;
import de.ole101.marketplace.common.menu.item.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;

@Data
@AllArgsConstructor
public class MenuContext {

    private Component title;
    private Set<MenuItem> menuItems;
    private Consumer<Player> closeConsumer;
    private String layout;

    public int getRows() {
        return this.layout.split("\n").length;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {

        private static TranslationService translationService;
        private final Set<MenuItem> menuItems = ConcurrentHashMap.newKeySet();
        private Component title = text("Menu");
        private Consumer<Player> closeConsumer;
        private String layout = """
                                #########
                                #########
                                #########
                                """;

        public Builder translated(String key) {
            return translated(key, context -> {});
        }

        public Builder translated(String key, Consumer<TranslationContext> consumer) {
            return title(getTranslationService().translate(key, consumer));
        }

        public Builder item(MenuItem menuItem) {
            this.menuItems.add(menuItem);
            return this;
        }

        public Builder item(ItemStack itemStack, String layoutId) {
            return item(itemStack, layoutId, null);
        }

        public Builder item(ItemStack itemStack, String layoutId, Consumer<Click> function) {
            return item(MenuItem.builder()
                    .itemStack(itemStack)
                    .function(function)
                    .layoutId(layoutId)
                    .build());
        }

        public MenuContext build() {
            return new MenuContext(this.title, this.menuItems, this.closeConsumer, this.layout);
        }

        private TranslationService getTranslationService() {
            if (translationService == null) {
                translationService = MarketplacePlugin.getPlugin().getInjector().getInstance(TranslationService.class);
            }
            return translationService;
        }
    }
}
