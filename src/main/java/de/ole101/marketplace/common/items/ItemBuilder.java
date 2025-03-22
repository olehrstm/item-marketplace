package de.ole101.marketplace.common.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.ole101.marketplace.MarketplacePlugin;
import de.ole101.marketplace.common.i18n.TranslationContext;
import de.ole101.marketplace.common.i18n.TranslationService;
import de.ole101.marketplace.common.menu.item.MenuItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static de.ole101.marketplace.MarketplacePlugin.MM;
import static java.util.Optional.ofNullable;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static net.kyori.adventure.text.format.TextDecoration.State.FALSE;

public interface ItemBuilder {

    ItemStack FILL_ITEM = of(Material.GRAY_STAINED_GLASS_PANE)
            .displayName(empty())
            .data(itemStack -> itemStack.setData(DataComponentTypes.HIDE_TOOLTIP))
            .build();

    MenuItem FILL_MENU_ITEM = MenuItem.builder()
            .itemStack(FILL_ITEM)
            .build();

    static Builder of(Material material) {
        return new Builder(material);
    }

    static Builder of(Material material, int amount) {
        return new Builder(material, amount);
    }

    static Builder of(ItemStack itemStack) {
        return new Builder(itemStack.clone());
    }

    class Builder {

        private static TranslationService translationService;
        private final ItemStack itemStack;
        private boolean hideAdditionalTooltip = true;

        public Builder(Material material) {
            this.itemStack = ItemStack.of(material);
        }

        public Builder(Material material, int amount) {
            this.itemStack = ItemStack.of(material, amount);
        }

        public Builder(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public Builder displayName(Component displayName) {
            return meta(itemMeta -> itemMeta.displayName(displayName.decorationIfAbsent(ITALIC, FALSE)));
        }

        public Builder translatedDisplayName(String key) {
            return translatedDisplayName(key, context -> {});
        }

        public Builder translatedDisplayName(String key, Consumer<TranslationContext> consumer) {
            return displayName(getTranslationService().translate(key, consumer));
        }

        public Builder lore(Component... lore) {
            return lore(List.of(lore));
        }

        public Builder lore(List<Component> lore) {
            this.itemStack.lore(lore.stream()
                    .map(component -> component.decorationIfAbsent(ITALIC, FALSE).colorIfAbsent(NamedTextColor.WHITE))
                    .toList());
            return this;
        }

        public Builder translatedLore(String key) {
            return translatedLore(key, context -> {});
        }

        public Builder translatedLore(String key, Consumer<TranslationContext> consumer) {
            Component component = getTranslationService().translate(key, consumer);

            String serialize = MM.serialize(component); // we need to pass each line separately, because lores don't accept new lines
            List<Component> lines = Arrays.stream(serialize.split("\n")).map(MM::deserialize).toList();

            return lore(lines);
        }

        public Builder amount(int amount) {
            this.itemStack.setAmount(amount);
            return this;
        }

        public Builder unbreakable() {
            return unbreakable(true);
        }

        public Builder unbreakable(boolean unbreakable) {
            return meta(itemMeta -> itemMeta.setUnbreakable(unbreakable));
        }

        public Builder customModelData(int customModelData) {
            return meta(itemMeta -> itemMeta.setCustomModelData(customModelData));
        }

        public Builder skullOwner(OfflinePlayer offlinePlayer) {
            return skullMeta(meta -> meta.setOwningPlayer(offlinePlayer));
        }

        public Builder skullValue(String value) {
            return skullMeta(meta -> {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                try {
                    textures.setSkin(new URI("https://textures.minecraft.net/texture/" + value).toURL());
                } catch (MalformedURLException | URISyntaxException e) {
                    throw new RuntimeException("Invalid texture id: " + value);
                }

                profile.setTextures(textures);
                meta.setPlayerProfile(profile);
            });
        }

        public Builder skullMeta(Consumer<? super SkullMeta> itemMeta) {
            return meta(meta -> {
                if (meta instanceof SkullMeta skullMeta) {
                    itemMeta.accept(skullMeta);
                }
            });
        }

        public Builder meta(Consumer<? super ItemMeta> itemMeta) {
            this.itemStack.editMeta(itemMeta);
            return this;
        }

        public Builder data(Consumer<? super ItemStack> itemStack) {
            itemStack.accept(this.itemStack);
            return this;
        }

        public Builder hideAdditionalTooltip(boolean hideAdditionalTooltip) {
            this.hideAdditionalTooltip = hideAdditionalTooltip;
            return this;
        }

        public <C> Builder persistentData(NamespacedKey namespacedKey, PersistentDataType<?, C> persistentDataType, C value) {
            return meta(meta -> meta.getPersistentDataContainer().set(namespacedKey, persistentDataType, value));
        }

        public ItemStack build() {
            if (this.hideAdditionalTooltip) {
                data(itemStack -> {
                    itemStack.setData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);

                    ItemAttributeModifiers itemAttributeModifiers = ofNullable(itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS))
                            .map(modifiers -> modifiers.showInTooltip(false))
                            .orElseThrow();

                    itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
                });
            }

            return this.itemStack;
        }

        private TranslationService getTranslationService() {
            if (translationService == null) {
                translationService = MarketplacePlugin.getPlugin().getInjector().getInstance(TranslationService.class);
            }
            return translationService;
        }
    }
}
