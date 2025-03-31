package de.ole101.marketplace.services;

import com.google.inject.Inject;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.Transaction;
import de.ole101.marketplace.common.models.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.List;

public class MarketplaceService {

    private final UserService userService;
    private final PlayerService playerService;
    private final TranslationService translationService;

    @Inject
    public MarketplaceService(UserService userService, PlayerService playerService, TranslationService translationService) {
        this.userService = userService;
        this.playerService = playerService;
        this.translationService = translationService;
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
                .seller(player.getUniqueId())
                .build();
        user.getOffers().add(offer);

        this.userService.update(user);
    }

    public void buyOffer(Player buyer, Offer offer) {
        User buyerUser = this.playerService.getUser(buyer);
        User sellerUser = getUserByOffer(offer);

        if (sellerUser == null || buyerUser == null) {
            return;
        }

        boolean hasEnoughMoney = buyerUser.getBalance() >= offer.getPrice();
        if (offer.isBought() || !hasEnoughMoney) {
            return;
        }

        ItemStack itemStack = offer.getItemStack();
        buyer.getInventory().addItem(itemStack);

        offer.setBoughtAt(Instant.now());
        offer.setBuyer(buyer.getUniqueId());

        sellerUser.getOffers().remove(offer);
        sellerUser.getTransactions().add(Transaction.builder()
                .type(Transaction.Type.SELL)
                .offer(offer)
                .build());
        buyerUser.getTransactions().add(Transaction.builder()
                .type(Transaction.Type.BUY)
                .offer(offer)
                .build());

        buyerUser.setBalance(buyerUser.getBalance() - offer.getPrice());
        sellerUser.setBalance(sellerUser.getBalance() + offer.getPrice());

        // Update the users
        this.userService.update(buyerUser);
        this.userService.update(sellerUser);

        this.translationService.send(buyer, "marketplace.buy.success", context -> context.withNumber("price", offer.getPrice())
                .with("itemName", itemStack.effectiveName())
                .withNumber("itemAmount", itemStack.getAmount())
                .with("seller", sellerUser.getOfflinePlayer().getName()));

        Player seller = sellerUser.getPlayer();
        if (seller != null) {
            this.translationService.send(seller, "marketplace.sell.success", context -> context.withNumber("price", offer.getPrice())
                    .with("itemName", itemStack.effectiveName())
                    .withNumber("itemAmount", itemStack.getAmount())
                    .with("buyer", buyerUser.getOfflinePlayer().getName()));
        }
    }
}
