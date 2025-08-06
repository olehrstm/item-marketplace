package de.ole101.marketplace.common;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.command.CommandBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class Registry {

    private final MarketplacePlugin plugin;

    @Inject
    public Registry(MarketplacePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAllCommands(LifecycleEventManager<@NotNull Plugin> lifecycleEventManager) {
        AtomicInteger successCases = new AtomicInteger();
        List<Class<?>> commandClasses = getAllClassesFromPackage("command.commands").stream()
                .filter(CommandBase.class::isAssignableFrom)
                .toList();

        commandClasses.forEach(commandClass -> {
            try {
                CommandBase commandBase = (CommandBase) commandClass.getConstructor().newInstance();

                this.plugin.getInjector().injectMembers(commandBase);

                lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS,
                        event -> event.registrar().register(
                                commandBase.builder()
                                        .requires(sourceStack -> testAccess(sourceStack, commandBase.getPermission()))
                                        .build(),
                                commandBase.getDescription(),
                                List.of(commandBase.getAliases())));

                successCases.getAndIncrement();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                log.warn("Failed to register command {}: {}", commandClass.getSimpleName(), e.getMessage());
            }
        });

        log.info("Registered commands: {}/{}", successCases.get(), commandClasses.size());
    }

    private Set<Class<?>> getAllClassesFromPackage(String packageName) {
        try {
            return ClassPath.from(this.plugin.getClass().getClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName().startsWith("de.ole101.marketplace." + packageName))
                    .map(ClassPath.ClassInfo::load)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("Failed to get all classes from package: {}", packageName, e);
            return Set.of();
        }
    }

    private boolean testAccess(CommandSourceStack source, String permission) {
        if (!(source.getSender() instanceof Player player)) {
            return false;
        }

        return player.hasPermission(permission);
    }
}
