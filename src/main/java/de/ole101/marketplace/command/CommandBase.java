package de.ole101.marketplace.command;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.services.PlayerService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.adventure.PaperAdventure.asVanilla;
import static net.kyori.adventure.text.Component.text;

@Slf4j
@Getter
@Setter
public abstract class CommandBase {

    private final String label;
    private final String[] aliases;
    private String description;
    private String permission;

    @Inject
    private PlayerService playerService; // TODO: inject this properly

    protected CommandBase(String label, String... aliases) {
        this.label = label;
        this.aliases = aliases;
    }

    public abstract @NotNull LiteralArgumentBuilder<CommandSourceStack> builder();

    public SimpleCommandExceptionType error(Component component) {
        return new SimpleCommandExceptionType(asVanilla(component));
    }

    public Player player(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getSender() instanceof Player player) {
            return player;
        } else {
            throw error(text("not player", NamedTextColor.RED)).create(); // TODO: use translation
        }
    }

    public User user(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = player(context);
        User user = this.playerService.getUser(player);
        if (user == null) {
            throw error(text("not player", NamedTextColor.RED)).create(); // TODO: use translation
        }
        return user;
    }
}
