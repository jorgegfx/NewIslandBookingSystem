package com.newisland.user.service;

import com.newisland.user.model.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findById(UUID id);
    User findByEmailOrCreate(String email,String name);
    User save(User user);
    User update(User user);
    void delete(UUID id);
}
