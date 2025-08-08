package de.ole101.marketplace.services;

import de.ole101.marketplace.common.models.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User create(UUID uniqueId);

    Optional<User> userById(ObjectId id);

    List<User> allUsers();

    void update(User user);

    void delete(User user);
}

