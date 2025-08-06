package de.ole101.marketplace.repositories;

import de.ole101.marketplace.common.models.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void insert(User user);

    Optional<User> userById(ObjectId id);

    List<User> allUsers();

    void update(User user);

    void delete(User user);
}
