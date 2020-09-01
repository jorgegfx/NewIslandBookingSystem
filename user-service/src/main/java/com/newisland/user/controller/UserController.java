package com.newisland.user.controller;

import com.newisland.user.dto.CreateUserRequest;
import com.newisland.user.model.entity.User;
import com.newisland.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return Mono.justOrEmpty(userService.findById(UUID.fromString(id)));
    }

    @PostMapping
    private Mono<User> save(@RequestBody CreateUserRequest createUserRequest){
        try {
            return Mono.justOrEmpty(
                    userService.save(createUserRequest.toDomain()));
        }catch (Exception ex){
            String errorMessage = String.format("Error Creating user: %s ...",createUserRequest);
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
