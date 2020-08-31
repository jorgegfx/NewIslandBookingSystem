package com.newisland.user.controller;

import com.newisland.user.model.entity.User;
import com.newisland.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(UserController.class)
class UserControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private UserService userService;


    @Test
    public void testFindUserById(){
        UUID id = UUID.randomUUID();
        User user = User.builder().name("Test").email("test@test.com").build();
        when(userService.findById(id)).thenReturn(Optional.of(user));
        webTestClient.get()
                .uri("/user/"+id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(resUser -> resUser.getName(), equalTo("Test"));
    }
}