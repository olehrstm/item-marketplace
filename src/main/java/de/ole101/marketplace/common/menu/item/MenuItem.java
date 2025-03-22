package de.ole101.marketplace.common.menu.item;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

@Data
@Builder
@Accessors(chain = true)
public class MenuItem {

    @Builder.Default
    private UUID uniqueId = UUID.randomUUID();
    @Builder.Default
    private int slot = -1;
    private ItemStack itemStack;
    private Consumer<Click> function;
    private String layoutId;
}
