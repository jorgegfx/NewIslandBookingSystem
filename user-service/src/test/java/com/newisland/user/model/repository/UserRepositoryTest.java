package com.newisland.user.model.repository;

import com.newisland.user.model.entity.User;
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
        User user = User.builder().name("Test").email("test@test.com").build();
        User expectedUser = userRepository.save(user);
        assertThat(userRepository.findById(user.getId())).hasValue(expectedUser);
    }
}