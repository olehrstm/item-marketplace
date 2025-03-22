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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.network.chat.Component.translatableEscape;

public class ListEntryArgument<E> implements CustomArgumentType<E, String> {

    private static final DynamicCommandExceptionType ERROR_ELEMENT_NOT_FOUND = new DynamicCommandExceptionType(
            value -> translatableEscape("argument.enum.invalid", value)
    );

    private final Map<String, E> elements;

    public ListEntryArgument(Map<String, E> elements) {
        this.elements = elements;
    }

    public @NotNull E parse(StringReader stringReader) throws CommandSyntaxException {
        String arg = stringReader.readUnquotedString();
        return this.elements.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(arg))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> ERROR_ELEMENT_NOT_FOUND.create(arg));
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
            @NotNull SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider
                ? suggest(this.elements.keySet().stream().toList(), builder)
                : empty();
    }

    public static <E> ListEntryArgument<E> listEntryArgument(Map<String, E> elements) {
        return new ListEntryArgument<>(elements);
    }
}
