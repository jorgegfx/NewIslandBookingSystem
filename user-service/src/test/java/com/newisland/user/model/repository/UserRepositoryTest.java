package com.newisland.user.model.repository;

import com.newisland.user.TestApp;
import com.newisland.user.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest(classes = TestApp.class)
class UserRepositoryTest {
    @Autowired private UserRepository userRepository;

    @Test
    public void findSavedUserById() {
        User user = User.builder().
                name("Test").
                email("test@test.com").
                createdOn(Instant.now()).build();
        User expectedUser = userRepository.save(user);
        assertThat(userRepository.findById(user.getId())).hasValue(expectedUser);
    }
}