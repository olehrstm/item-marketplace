package de.ole101.marketplace.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.ole101.marketplace.command.CommandBase;
import de.ole101.marketplace.services.MarketplaceService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

public class SellCommand extends CommandBase {

    @Inject
    private MarketplaceService marketplaceService;

    public SellCommand() {
        super("sell");
        setDescription("Lists the item in players hand in the marketplace for sale.");
        setPermission("marketplace.sell");
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> builder() {
        return Commands.literal(getLabel())
                .then(argument("price", LongArgumentType.longArg(1))
                        .executes(context -> {
                            Player player = player(context);
                            long price = LongArgumentType.getLong(context, "price");

                            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                            if (itemInMainHand.getType() == Material.AIR) {
                                this.translationService.send(player, "command.sell.noItem");
                                return SINGLE_SUCCESS;
                            }

                            this.marketplaceService.createOffer(player, itemInMainHand, price);
                            player.getInventory().setItemInMainHand(ItemStack.empty());
                            this.translationService.send(player, "command.sell.success",
                                    ctx -> ctx.withNumber("price", price));

                            return SINGLE_SUCCESS;
                        }));
    }
}

