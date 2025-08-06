package de.ole101.marketplace.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @NotNull
    @Builder.Default
    private ObjectId id = new ObjectId();

    @NotNull
    private UUID uniqueId;

    @NotNull
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

    @NotNull
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    private long balance;

    @BsonIgnore
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uniqueId);
    }

    @BsonIgnore
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.uniqueId);
    }
}