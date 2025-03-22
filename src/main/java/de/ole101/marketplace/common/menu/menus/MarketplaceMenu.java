package de.ole101.marketplace.common.menu.menus;

import de.ole101.marketplace.common.items.ItemBuilder;
import de.ole101.marketplace.common.menu.item.MenuItem;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenu;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenuContext;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.services.MarketplaceService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class MarketplaceMenu extends PaginatedMenu<Offer> {

    private final MarketplaceService marketplaceService;

    public MarketplaceMenu(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Override
    public PaginatedMenuContext<Offer> getMenu(Player player) {
        return PaginatedMenuContext.<Offer>paginated()
                .rows(6)
                .translated("menu.marketplace.title")
                .iterable(this.marketplaceService.getOffers().stream().sorted(Comparator.comparing(Offer::getCreatedAt).reversed()).toList())
                .itemProvider(offer -> MenuItem.builder()
                        .itemStack(ItemBuilder.of(offer.getItemStack())
                                .appendLore(empty(), text("--------------------"), text("Price: " + offer.getPrice()), text("Seller: " + this.marketplaceService.getUserByOffer(offer).getOfflinePlayer().getName()))
                                .build())
                        .build())
                .item(6, 3, ItemBuilder.of(Material.CLOCK).translatedDisplayName("menu.page.previous").build(), click -> previousPage())
                .item(6, 5, ItemBuilder.of(Material.PAPER).displayName(text(this.page + 1)).build())
                .item(6, 7, ItemBuilder.of(Material.CLOCK).translatedDisplayName("menu.page.next").build(), click -> nextPage())
                .fillItem(ItemBuilder.FILL_MENU_ITEM)
                .build();
    }
}
