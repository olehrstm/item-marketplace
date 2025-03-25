package de.ole101.marketplace.common.menu.menus;

import de.ole101.marketplace.common.menu.Menu;
import de.ole101.marketplace.common.menu.MenuContext;
import de.ole101.marketplace.common.models.Offer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public class ConfirmMenu extends Menu {

    private final MarketplaceMenu parent;
    private final Offer offer;

    public ConfirmMenu(MarketplaceMenu parent, Offer offer) {
        this.parent = parent;
        this.offer = offer;
    }

    @Override
    public MenuContext getMenu(Player player) {
        return MenuContext.builder()
                .menuId("marketplace.confirm")
                .item("a", click -> {
                    this.parent.getMarketplaceService().buyOffer(player, this.offer);
                    close(player);
                })
                .item("b", null, context -> context.withNumber("price", this.offer.getPrice())
                        .with("seller", this.parent.getMarketplaceService().getUserByOffer(this.offer).getOfflinePlayer().getName())
                        .withNumber("itemAmount", this.offer.getItemStack().getAmount())
                        .with("itemName", PlainTextComponentSerializer.plainText().serialize(this.offer.getItemStack().effectiveName())))
                .item("c", click -> this.parent.open(player))
                .build();
    }
}
