package de.ole101.marketplace.services;

import com.google.inject.Inject;
import de.ole101.marketplace.common.models.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Getter
public class PlayerService {

    private final Map<UUID, User> users = new HashMap<>();
    private final List<User> onlineUsers = new ArrayList<>();
    private final UserService userService;

    @Inject
    public PlayerService(UserService userService) {
        this.userService = userService;
    }

    public void loadAllUsers() {
        this.userService.allUsers().forEach(user -> this.users.put(user.getUniqueId(), user));
        log.info("Loaded {} users!", this.users.size()); // TODO: dont load all users at startup
    }

    public void registerPlayer(Player player) {
        UUID uniqueId = player.getUniqueId();
        User user = getUser(uniqueId);
        if (!this.onlineUsers.contains(user)) {
            this.onlineUsers.add(user);
        }
    }

    public void disposePlayer(Player player) {
        UUID uniqueId = player.getUniqueId();
        User user = getUser(uniqueId);

        this.userService.update(user);
        this.onlineUsers.remove(user);
    }

    public User getUser(UUID uniqueId) {
        return Optional.ofNullable(this.users.get(uniqueId))
                .orElseGet(() -> {
                    User user = this.userService.create(uniqueId);
                    this.users.put(uniqueId, user);

                    return user;
                });
    }

    public User getUser(OfflinePlayer offlinePlayer) {
        return getUser(offlinePlayer.getUniqueId());
    }
}

