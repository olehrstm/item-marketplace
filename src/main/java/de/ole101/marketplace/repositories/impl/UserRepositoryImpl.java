package de.ole101.marketplace.repositories.impl;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Optional.ofNullable;

public class UserRepositoryImpl implements UserRepository {

    private static final String USER_COLLECTION_NAME = "users";
    private static final String ID_FIELD_NAME = "_id";
    private final MongoCollection<User> mongoCollection;

    @Inject
    public UserRepositoryImpl(@NotNull MongoDatabase mongoDatabase) {
        this.mongoCollection = mongoDatabase.getCollection(USER_COLLECTION_NAME, User.class);
    }

    @Override
    public void insert(User user) {
        this.mongoCollection.insertOne(user);
    }

    @Override
    public Optional<User> userById(ObjectId id) {
        return ofNullable(this.mongoCollection.find(eq(ID_FIELD_NAME, id)).first());
    }

    @Override
    public List<User> allUsers() {
        return this.mongoCollection.find().into(new ArrayList<>());
    }

    @Override
    public void update(User user) {
        this.mongoCollection.replaceOne(eq(ID_FIELD_NAME, user.getId()), user);
    }

    @Override
    public void delete(User user) {
        this.mongoCollection.deleteOne(eq(ID_FIELD_NAME, user.getId()));
    }
}