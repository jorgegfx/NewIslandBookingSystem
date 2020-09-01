package com.newisland.user.controller;

import com.newisland.user.dto.CreateUserRequest;
import com.newisland.user.model.entity.User;
import com.newisland.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return Mono.justOrEmpty(userService.findByUuid(id));
    }

    @PostMapping("/save")
    private Mono<User> save(@RequestBody CreateUserRequest createUserRequest){
        return Mono.justOrEmpty(userService.save(createUserRequest.toDomain()));
    }
}
