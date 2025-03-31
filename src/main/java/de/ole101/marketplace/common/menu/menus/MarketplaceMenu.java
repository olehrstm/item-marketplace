package de.ole101.marketplace.common.menu.menus;

import de.ole101.marketplace.common.items.ItemBuilder;
import de.ole101.marketplace.common.menu.item.MenuItem;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenu;
import de.ole101.marketplace.common.menu.pagination.PaginatedMenuContext;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.services.MarketplaceService;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static de.ole101.marketplace.common.models.Offer.Type.MARKETPLACE;

public class MarketplaceMenu extends PaginatedMenu<Offer> {

    @Getter
    private final MarketplaceService marketplaceService;
    private final User user;

    public MarketplaceMenu(MarketplaceService marketplaceService, User user) {
        this.marketplaceService = marketplaceService;
        this.user = user;
    }

    @Override
    public PaginatedMenuContext<Offer> getMenu(Player player) {
        return PaginatedMenuContext.<Offer>paginated()
                .menuId("marketplace")
                .iterable(this.marketplaceService.getOffers().stream().filter(offer -> offer.getType() == MARKETPLACE).filter(offer -> !offer.isBought()).sorted(Comparator.comparing(Offer::getCreatedAt).reversed()).toList())
                .itemProvider(offer -> {
                    ItemBuilder.Builder builder = ItemBuilder.of(offer.getItemStack());

                    List<Component> lore = offer.getItemStack().lore();
                    User seller = this.marketplaceService.getUserByOffer(offer);
                    if (lore == null || lore.isEmpty()) {
                        builder.translatedLore("menu.marketplace.item.lore.empty", context -> context.withNumber("price", offer.getPrice())
                                .with("seller", seller.getOfflinePlayer().getName())
                                .withDateTime("createdAt", LocalDateTime.ofInstant(offer.getCreatedAt(), ZoneId.systemDefault()))
                        );
                    } else {
                        builder.translatedLore("menu.marketplace.item.lore", context -> context.withNumber("price", offer.getPrice())
                                .with("seller", seller.getOfflinePlayer().getName())
                                .withDateTime("createdAt", LocalDateTime.ofInstant(offer.getCreatedAt(), ZoneId.systemDefault()))
                                .with("existingLore", lore)
                        );
                    }

                    return MenuItem.builder()
                            .itemStack(builder.build())
                            .function(click -> {
                                //                                if (player.getUniqueId().equals(seller.getOfflinePlayer().getUniqueId())) {
                                //                                    return;
                                //                                }

                                if (this.user.getBalance() < offer.getPrice()) {
                                    return;
                                }

                                new ConfirmMenu(this, offer).open(player);
                            })
                            .build();
                })
                .itemId("1")
                .item("a", click -> previousPage())
                .item("b", context -> context.with("current", this.page + 1), null)
                .item("c", click -> nextPage())
                .build();
    }
}
