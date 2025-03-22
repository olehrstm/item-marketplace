package de.ole101.marketplace.services;

import com.google.inject.Inject;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MarketplaceService {

    private final UserService userService;
    private final PlayerService playerService;

    @Inject
    public MarketplaceService(UserService userService, PlayerService playerService) {
        this.userService = userService;
        this.playerService = playerService;
    }

    public List<Offer> getOffers() {
        return this.playerService.getUsers().values().stream()
                .map(User::getOffers)
                .flatMap(List::stream)
                .toList();
    }

    public User getUserByOffer(Offer offer) {
        return this.playerService.getUsers().values().stream()
                .filter(user -> user.getOffers().contains(offer))
                .findFirst()
                .orElse(null);
    }

    public void createOffer(Player player, ItemStack itemStack, long price) {
        User user = this.playerService.getUser(player);

        Offer offer = Offer.builder()
                .itemStack(itemStack)
                .price(price)
                .type(Offer.Type.MARKETPLACE)
                .build();
        user.getOffers().add(offer);

        this.userService.update(user);
    }
}
