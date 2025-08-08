package de.ole101.marketplace.common.wrapper;

import com.google.inject.Inject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.ole101.marketplace.common.configurations.Configuration;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
@Getter
@Accessors(fluent = true)
public class MongoWrapper {

    public final MongoClient mongoClient;
    public final MongoDatabase mongoDatabase;

    @Inject
    public MongoWrapper(Configuration config) {
        CodecProvider pojoCodedProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromCodecs(new InstantCodec(), new ItemStackCodec()),
                fromProviders(pojoCodedProvider)
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(config.getMongoUri()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(pojoCodecRegistry)
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.mongoDatabase = this.mongoClient.getDatabase(config.getMongoDatabaseName());
        log.info("Connected to MongoDB!");
    }

    public void close() {
        this.mongoClient.close();
        log.info("Closed MongoDB connection!");
    }

    public static class InstantCodec implements Codec<Instant> {

        @Override
        public void encode(BsonWriter writer, Instant value, EncoderContext encoderContext) {
            writer.writeDateTime(value.toEpochMilli());
        }

        @Override
        public Class<Instant> getEncoderClass() { return Instant.class; }

        @Override
        public Instant decode(BsonReader reader, DecoderContext decoderContext) {
            return Instant.ofEpochMilli(reader.readDateTime());
        }
    }


    public static class ItemStackCodec implements Codec<ItemStack> {

        @Override
        public void encode(BsonWriter writer, ItemStack value, EncoderContext encoderContext) {
            try {
                byte[] bytes = value.serializeAsBytes();
                writer.writeBinaryData(new BsonBinary(bytes));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize ItemStack", e);
            }
        }

        @Override
        public Class<ItemStack> getEncoderClass() { return ItemStack.class; }

        @Override
        public ItemStack decode(BsonReader reader, DecoderContext decoderContext) {
            try {
                byte[] bytes = reader.readBinaryData().getData();
                return ItemStack.deserializeBytes(bytes);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize ItemStack", e);
            }
        }
    }
}

