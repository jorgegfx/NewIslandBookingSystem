package com.newisland.user.service;

import com.newisland.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findById(UUID id);
    void save(User user);
}
