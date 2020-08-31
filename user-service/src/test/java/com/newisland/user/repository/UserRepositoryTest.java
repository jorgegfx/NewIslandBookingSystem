package com.newisland.user.repository;

import com.newisland.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
class UserRepositoryTest {
    @Autowired private UserRepository userRepository;

    @Test
    public void findSavedUserById() {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@test.com");
        user.setCreatedOn(Instant.now());
        User expectedUser = userRepository.save(user);
        assertThat(userRepository.findById(user.getId())).hasValue(expectedUser);
    }
}