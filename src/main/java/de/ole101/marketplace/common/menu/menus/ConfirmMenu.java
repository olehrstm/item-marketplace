package de.ole101.marketplace.common.menu.menus;

import de.ole101.marketplace.common.menu.Menu;
import de.ole101.marketplace.common.menu.MenuContext;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.services.MarketplaceService;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class ConfirmMenu extends Menu {

    private final Menu parent;
    private final Offer offer;
    private final MarketplaceService marketplaceService;

    public ConfirmMenu(Menu parent, MarketplaceService marketplaceService, Offer offer) {
        this.parent = parent;
        this.offer = offer;
        this.marketplaceService = marketplaceService;
    }

    @Override
    public MenuContext getMenu(Player player) {
        return MenuContext.builder()
                .menuId("marketplace.confirm")
                .item("a", click -> {
                    this.marketplaceService.buyOffer(player, this.offer);
                    close(player);
                })
                .item("b", null, context -> {
                    User userByOffer = this.marketplaceService.getUserByOffer(this.offer);

                    context.withNumber("price", this.offer.getPrice())
                            .with("seller", userByOffer == null ? "/" : userByOffer.getOfflinePlayer().getName())
                            .withNumber("itemAmount", this.offer.getItemStack().getAmount())
                            .with("itemName", PlainTextComponentSerializer.plainText().serialize(this.offer.getItemStack().effectiveName()));
                })
                .item("c", click -> this.parent.open(player))
                .build();
    }
}
