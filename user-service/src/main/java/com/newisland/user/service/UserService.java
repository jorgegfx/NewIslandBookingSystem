package com.newisland.user.service;

import com.newisland.user.model.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findByUuid(String uuid);
    User save(User user);
}
