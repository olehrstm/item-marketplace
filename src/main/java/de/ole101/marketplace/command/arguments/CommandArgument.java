package de.ole101.marketplace.command.arguments;

@FunctionalInterface
public interface CommandArgument {

    /**
     * Returns the string representation of the enum value for tab completion.
     *
     * @return The string representation of the enum value for tab completion.
     */
    String tabString();
}