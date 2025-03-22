package de.ole101.marketplace.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static java.util.Arrays.stream;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.network.chat.Component.translatableEscape;

public class EnumArgument<E extends Enum<E> & CommandArgument> implements CustomArgumentType<E, String> {

    private static final DynamicCommandExceptionType ERROR_ENUM_NOT_FOUND = new DynamicCommandExceptionType(
            value -> translatableEscape("argument.enum.invalid", value)
    );

    private final Class<E> enumType;

    public EnumArgument(Class<E> enumType) {
        this.enumType = enumType;
    }

    public @NotNull E parse(StringReader stringReader) throws CommandSyntaxException {
        String arg = stringReader.readUnquotedString();
        return stream(this.enumType.getEnumConstants())
                .filter(enumConstant -> enumConstant.tabString().equalsIgnoreCase(arg))
                .findFirst()
                .orElseThrow(() -> ERROR_ENUM_NOT_FOUND.create(arg));
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
            @NotNull SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider
                ? suggest(stream(this.enumType.getEnumConstants()).map(CommandArgument::tabString).toList(), builder)
                : empty();
    }

    public static <E extends Enum<E> & CommandArgument> EnumArgument<E> enumArgument(Class<E> enumType) {
        return new EnumArgument<>(enumType);
    }
}
