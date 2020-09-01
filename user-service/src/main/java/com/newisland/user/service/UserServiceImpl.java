package com.newisland.user.service;

import com.newisland.user.model.entity.User;
import com.newisland.user.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUuid(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    @Override
    public User save(User user) {
        user.setUuid(UUID.randomUUID().toString());
        user.setCreatedOn(Instant.now());
        userRepository.save(user);
        return user;
    }
}
