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
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByEmailOrCreate(String email, String name) {
        Optional<User> res = userRepository.findByEmail(email);
        if(res.isPresent())
            return res.get();
        return this.save(User.builder().email(email).name(name).build());
    }

    @Override
    public User save(User user) {
        user.setCreatedOn(Instant.now());
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        user.setUpdatedOn(Instant.now());
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        this.findById(id).ifPresent(userRepository::delete);
    }
}
