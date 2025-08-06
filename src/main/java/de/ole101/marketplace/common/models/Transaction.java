package de.ole101.marketplace.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @NotNull
    @Builder.Default
    private ObjectId id = new ObjectId();

    @NotNull
    private Type type;

    @NotNull
    private Offer offer;

    public enum Type {
        BUY, SELL
    }
}