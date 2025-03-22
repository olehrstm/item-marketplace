package de.ole101.marketplace.services.impl;

import com.google.inject.Inject;
import de.ole101.marketplace.common.models.User;
import de.ole101.marketplace.repositories.UserRepository;
import de.ole101.marketplace.services.UserService;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(UUID uniqueId) {
        User user = User.builder()
                .uniqueId(uniqueId)
                .build();

        this.userRepository.insert(user);

        return user;
    }

    @Override
    public Optional<User> userById(ObjectId id) {
        return this.userRepository.userById(id);
    }

    @Override
    public List<User> allUsers() {
        return this.userRepository.allUsers();
    }

    @Override
    public void update(User user) {
        this.userRepository.update(user);
    }

    @Override
    public void delete(User user) {
        this.userRepository.delete(user);
    }
}
