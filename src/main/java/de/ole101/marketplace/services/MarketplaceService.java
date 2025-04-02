package de.ole101.marketplace.services;

import club.minnced.discord.webhook.send.WebhookEmbed;
import com.google.inject.Inject;
import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.configurations.Configuration;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.models.Offer;
import de.ole101.marketplace.common.models.Transaction;
import de.ole101.marketplace.common.models.User;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarketplaceService {

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();
    private final Configuration configuration;
    private final UserService userService;
    private final PlayerService playerService;
    private final TranslationService translationService;
    private final WebhookService webhookService;

    @Inject
    public MarketplaceService(Configuration configuration, UserService userService, PlayerService playerService, TranslationService translationService, WebhookService webhookService) {
        this.configuration = configuration;
        this.userService = userService;
        this.playerService = playerService;
        this.translationService = translationService;
        this.webhookService = webhookService;
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

        this.webhookService.sendEmbed("offer.created", context -> context.with("player", player.getName())
                .with("itemName", PLAIN_TEXT_SERIALIZER.serialize(itemStack.effectiveName()))
                .withNumber("itemAmount", itemStack.getAmount())
                .withNumber("price", price), builder -> builder.setTimestamp(Instant.now())
                .setAuthor(new WebhookEmbed.EmbedAuthor(player.getName(), String.format("https://minotar.net/helm/%s/100.png", player.getUniqueId()), "")));
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

        boolean isBlackMarket = offer.getType() == Offer.Type.BLACK_MARKET;
        long price = offer.getPrice();
        if (isBlackMarket) {
            // Reimburse the seller 2x if their item is sold on the black market
            price *= 2;
        }
        sellerUser.setBalance(sellerUser.getBalance() + price);

        // Update the users
        this.userService.update(buyerUser);
        this.userService.update(sellerUser);

        long finalPrice = price;
        this.translationService.send(buyer, "marketplace.buy.success", context -> context.withNumber("price", finalPrice)
                .with("itemName", itemStack.effectiveName())
                .withNumber("itemAmount", itemStack.getAmount())
                .with("seller", sellerUser.getOfflinePlayer().getName()));

        Player seller = sellerUser.getPlayer();
        if (seller != null) {
            if (isBlackMarket) {
                this.translationService.send(seller, "marketplace.sell.blackMarket.success", context -> context.withNumber("price", finalPrice)
                        .with("itemName", itemStack.effectiveName())
                        .withNumber("itemAmount", itemStack.getAmount())
                        .with("buyer", buyerUser.getOfflinePlayer().getName()));
            } else {
                this.translationService.send(seller, "marketplace.sell.success", context -> context.withNumber("price", finalPrice)
                        .with("itemName", itemStack.effectiveName())
                        .withNumber("itemAmount", itemStack.getAmount())
                        .with("buyer", buyerUser.getOfflinePlayer().getName()));
            }
        }

        this.webhookService.sendEmbed(isBlackMarket ? "item.bought.blackMarket" : "item.bought", context -> context.with("player", finalPrice)
                .with("seller", sellerUser.getOfflinePlayer().getName())
                .with("itemName", PLAIN_TEXT_SERIALIZER.serialize(itemStack.effectiveName()))
                .withNumber("itemAmount", itemStack.getAmount())
                .withNumber("price", offer.getPrice()), builder -> builder.setTimestamp(Instant.now())
                .setAuthor(new WebhookEmbed.EmbedAuthor(buyer.getName(), String.format("https://minotar.net/helm/%s/100.png", buyer.getUniqueId()), "")));
    }

    public void scheduleBlackMarketRefresh() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(MarketplacePlugin.getPlugin(), () -> refreshBlackMarket(this.configuration.getMaxBlackMarketItems()), 0L, this.configuration.getBlackMarketRefreshInterval() * 20L);
    }

    public void refreshBlackMarket(long maxItems) {
        List<Offer> offers = getOffers();

        if (offers.isEmpty()) {
            return;
        }

        for (Offer offer : offers) {
            if (offer.getType() == Offer.Type.BLACK_MARKET) {
                offer.setType(Offer.Type.MARKETPLACE);
                offer.setPrice(offer.getPrice() * 2);
                User userByOffer = this.getUserByOffer(offer);
                this.userService.update(userByOffer);
            }
        }

        List<Offer> filteredOffers = offers.stream()
                .filter(offer -> offer.getType() == Offer.Type.MARKETPLACE)
                .collect(Collectors.toList());

        Collections.shuffle(filteredOffers);
        List<Offer> selectedOffers = filteredOffers.stream()
                .limit(maxItems)
                .toList();

        for (Offer offer : selectedOffers) {
            offer.setType(Offer.Type.BLACK_MARKET);
            offer.setPrice(offer.getPrice() / 2);
            User userByOffer = this.getUserByOffer(offer);
            this.userService.update(userByOffer);

            Player player = userByOffer.getPlayer();
            if (player != null) {
                this.translationService.send(player, "blackMarket.refresh", context -> context.with("itemName", offer.getItemStack().effectiveName())
                        .withNumber("itemAmount", offer.getItemStack().getAmount()));
            }
        }

        this.webhookService.sendEmbed("blackMarket.refresh", builder -> builder.setTimestamp(Instant.now()));
    }
}
