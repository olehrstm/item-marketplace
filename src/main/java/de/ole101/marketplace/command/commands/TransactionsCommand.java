package de.ole101.marketplace.command.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.ole101.marketplace.command.CommandBase;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.Transaction;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.services.PlayerService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

public class TransactionsCommand extends CommandBase {

    @Inject
    private PlayerService playerService;

    public TransactionsCommand() {
        super("transactions");
        setDescription("Displays a player's transaction history.");
        setPermission("marketplace.history");
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> builder() {
        return Commands.literal(getLabel())
                .then(argument("player", ArgumentTypes.playerProfiles())
                        .executes(context -> {
                            Player player = player(context);
                            PlayerProfileListResolver profilesResolver = context.getArgument(
                                    "player",
                                    PlayerProfileListResolver.class
                            );
                            Collection<PlayerProfile> foundProfiles = profilesResolver.resolve(
                                    context.getSource()
                            );

                            if (foundProfiles.isEmpty()) {
                                this.translationService.send(player, "error.command.no.profile");
                                return SINGLE_SUCCESS;
                            }

                            PlayerProfile profile = foundProfiles.iterator().next();
                            UUID uniqueId = profile.getId();

                            User user = this.playerService.getUser(uniqueId);
                            List<Transaction> transactions = user.getTransactions().stream()
                                    .sorted(Comparator.comparing(
                                            transaction -> transaction.getOffer().getBoughtAt()
                                    ))
                                    .toList();

                            if (transactions.isEmpty()) {
                                this.translationService.send(player, "command.transactions.empty");
                                return SINGLE_SUCCESS;
                            }

                            this.translationService.send(player, "command.transactions.header",
                                    ctx -> ctx.with("player", profile.getName()));

                            transactions.forEach(transaction -> {
                                Offer offer = transaction.getOffer();

                                this.translationService.send(player, "command.transactions.entry",
                                        ctx -> ctx.with("type", transaction.getType().name())
                                                .with("itemName", offer.getItemStack()
                                                        .effectiveName())
                                                .withNumber("price", offer.getPrice())
                                                .withDateTime("boughtAt",
                                                        LocalDateTime.ofInstant(offer.getBoughtAt(),
                                                                ZoneId.systemDefault()
                                                        )
                                                )
                                                .with("transactionId", transaction.getId()));
                            });

                            return SINGLE_SUCCESS;
                        }));
    }
}

