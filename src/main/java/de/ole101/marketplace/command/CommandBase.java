package de.ole101.marketplace.command;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.services.PlayerService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.adventure.PaperAdventure.asVanilla;

@Slf4j
@Getter
@Setter
public abstract class CommandBase {

    @Inject
    protected PlayerService playerService;
    @Inject
    protected TranslationService translationService;
    private final String label;
    private final String[] aliases;
    private String description;
    private String permission;

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
            throw error(this.translationService.translate("error.command.not.player")).create();
        }
    }

    public User user(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = player(context);
        User user = this.playerService.getUser(player);
        if (user == null) {
            throw error(this.translationService.translate("error.command.not.player")).create();
        }
        return user;
    }
}
