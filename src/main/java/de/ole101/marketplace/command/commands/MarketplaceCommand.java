package de.ole101.marketplace.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.ole101.marketplace.command.CommandBase;
import de.ole101.marketplace.common.menu.menus.MarketplaceMenu;
import de.ole101.marketplace.services.MarketplaceService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class MarketplaceCommand extends CommandBase {

    @Inject
    private MarketplaceService marketplaceService;

    public MarketplaceCommand() {
        super("marketplace");
        setDescription("Displays a list of all items currently for sale in the marketplace.");
        setPermission("marketplace.view");
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> builder() {
        return Commands.literal(getLabel()).executes(context -> {
            Player player = player(context);
            new MarketplaceMenu(this.marketplaceService).open(player);

            return SINGLE_SUCCESS;
        });
    }
}
