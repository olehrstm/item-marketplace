package de.ole101.marketplace.common.models;

import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @NotNull
    @Builder.Default
    private final ObjectId id = new ObjectId();

    @NotNull
    private ItemStack itemStack;

    private long price;

    @Nullable
    private UUID buyer;

    @NotNull
    private Type type;

    @NotNull
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Nullable
    private Instant boughtAt;

    public enum Type {
        MARKETPLACE, BLACK_MARKET
    }
}
