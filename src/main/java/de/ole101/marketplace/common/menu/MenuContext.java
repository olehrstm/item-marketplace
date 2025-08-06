package de.ole101.marketplace.common.menu;

import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.configurations.MenuConfiguration;
import de.ole101.marketplace.common.i18n.TranslationContext;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.items.ItemBuilder;
import de.ole101.marketplace.common.menu.item.Click;
import de.ole101.marketplace.common.menu.item.MenuItem;
import de.ole101.marketplace.services.MenuService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
public class MenuContext {

    private MenuConfiguration.Menu menuConfiguration;
    private Component title;
    private Set<MenuItem> menuItems;
    private Consumer<Player> closeConsumer;

    public int getRows() {
        return this.menuConfiguration.getLayout().length;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {

        private static MenuService menuService;
        private static TranslationService translationService;
        private final Set<MenuItem> menuItems = ConcurrentHashMap.newKeySet();
        private MenuConfiguration.Menu menuConfiguration;
        private Consumer<Player> closeConsumer;
        private String menuId;

        public Builder item(String id) {
            return item(id, null, null, null);
        }

        public Builder item(String id, Consumer<Click> function) {
            return item(id, null, null, function);
        }

        public Builder item(String id, Consumer<TranslationContext> nameContext,
                Consumer<TranslationContext> loreContext) {
            return item(id, nameContext, loreContext, null);
        }

        public Builder item(String id, Consumer<TranslationContext> nameContext,
                Consumer<TranslationContext> loreContext, Consumer<Click> function) {
            MenuConfiguration.MenuItem menuItem = getMenuConfiguration().getItems().stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst().orElseThrow();

            this.menuItems.add(MenuItem.builder()
                    .itemStack(ItemBuilder.of(menuItem.getMaterial())
                            .translatedDisplayName(menuItem.getDisplayNameKey(), nameContext)
                            .translatedLore(menuItem.getLoreKey(), loreContext)
                            .build())
                    .function(function)
                    .layoutId(menuItem.getId())
                    .build());
            return this;
        }

        public MenuContext build() {
            return new MenuContext(getMenuConfiguration(),
                    getTranslationService().translate(getMenuConfiguration().getTitleKey()), this.menuItems,
                    this.closeConsumer);
        }

        private MenuConfiguration.Menu getMenuConfiguration() {
            if (this.menuConfiguration == null) {
                this.menuConfiguration = getMenuService().getMenu(this.menuId);
            }
            return this.menuConfiguration;
        }

        private MenuService getMenuService() {
            if (menuService == null) {
                menuService = MarketplacePlugin.getPlugin().getInjector().getInstance(MenuService.class);
            }
            return menuService;
        }

        private TranslationService getTranslationService() {
            if (translationService == null) {
                translationService = MarketplacePlugin.getPlugin().getInjector().getInstance(TranslationService.class);
            }
            return translationService;
        }
    }
}

