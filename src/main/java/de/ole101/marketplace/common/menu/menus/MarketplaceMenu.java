package de.ole101.marketplace.common.menu.menus;

import de.ole101.marketplace.common.items.ItemBuilder;
import de.ole101.marketplace.common.menu.item.MenuItem;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenu;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenuContext;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.services.MarketplaceService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public class MarketplaceMenu extends PaginatedMenu<Offer> {

    private final MarketplaceService marketplaceService;

    public MarketplaceMenu(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @Override
    public PaginatedMenuContext<Offer> getMenu(Player player) {
        return PaginatedMenuContext.<Offer>paginated()
                .translated("menu.marketplace.title")
                .layout("""
                        #########
                        #1111111#
                        #1111111#
                        #1111111#
                        #1111111#
                        ##a#b#c##
                        """)
                .itemId("1")
                .iterable(this.marketplaceService.getOffers().stream().sorted(Comparator.comparing(Offer::getCreatedAt).reversed()).toList())
                .itemProvider(offer -> {
                    ItemBuilder.Builder builder = ItemBuilder.of(offer.getItemStack());

                    List<Component> lore = offer.getItemStack().lore();
                    if (lore == null || lore.isEmpty()) {
                        builder.translatedLore("menu.marketplace.item.lore.empty", context -> context.withNumber("price", offer.getPrice())
                                .with("seller", this.marketplaceService.getUserByOffer(offer).getOfflinePlayer().getName())
                                .withDateTime("createdAt", LocalDateTime.ofInstant(offer.getCreatedAt(), ZoneId.systemDefault()))
                        );
                    } else {
                        builder.translatedLore("menu.marketplace.item.lore", context -> context.withNumber("price", offer.getPrice())
                                .with("seller", this.marketplaceService.getUserByOffer(offer).getOfflinePlayer().getName())
                                .withDateTime("createdAt", LocalDateTime.ofInstant(offer.getCreatedAt(), ZoneId.systemDefault()))
                                .with("existingLore", lore)
                        );
                    }

                    return MenuItem.builder()
                            .itemStack(builder.build())
                            .build();
                })
                .item(ItemBuilder.of(Material.CLOCK).translatedDisplayName("menu.page.previous").build(), "a", click -> previousPage())
                .item(ItemBuilder.of(Material.PAPER).translatedDisplayName("menu.page.current", context -> context.with("current", this.page + 1)).build(), "b")
                .item(ItemBuilder.of(Material.CLOCK).translatedDisplayName("menu.page.next").build(), "c", click -> nextPage())
                .build();
    }
}
