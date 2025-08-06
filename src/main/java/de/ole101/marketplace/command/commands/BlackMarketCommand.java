package de.ole101.marketplace.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.ole101.marketplace.command.CommandBase;
import de.ole101.marketplace.common.menu.menus.BlackMarketMenu;
import de.ole101.marketplace.services.MarketplaceService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

public class BlackMarketCommand extends CommandBase {

    @Inject
    private MarketplaceService marketplaceService;

    public BlackMarketCommand() {
        super("blackmarket");
        setDescription(
                "Generates a new shop from the /marketplace menu with 50% discounted prices from a selection of random items. Reimburse the seller 2x if their item is sold on the black market.");
        setPermission("marketplace.blackmarket");
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> builder() {
        return Commands.literal(getLabel())
                .then(argument("max_items", LongArgumentType.longArg(1)).executes(context -> {
                    Player player = player(context);
                    long maxItems = LongArgumentType.getLong(context, "max_items");

                    this.marketplaceService.refreshBlackMarket(maxItems);

                    this.translationService.send(player, "command.blackMarket.refresh.success");

                    return SINGLE_SUCCESS;
                }))
                .executes(context -> {
                    Player player = player(context);

                    new BlackMarketMenu(this.marketplaceService, user(context)).open(player);

                    return SINGLE_SUCCESS;
                });
    }
}
