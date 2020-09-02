package com.newisland.user.controller;

import com.newisland.user.dto.CreateUserRequest;
import com.newisland.user.model.entity.User;
import com.newisland.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(code = HttpStatus.CREATED)
    private Mono<User> findByEmailOrCreate(@RequestBody CreateUserRequest createUserRequest) {
        try {
            return Mono.justOrEmpty(
                    userService.findByEmailOrCreate(
                            createUserRequest.getEmail(),createUserRequest.getName()));
        } catch (Exception ex) {
            String errorMessage = String.format("Error Creating user: %s ...", createUserRequest);
            log.error(errorMessage, ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        try {
            userService.delete(UUID.fromString(id));
            return Mono.empty().then();
        } catch (Exception ex) {
            String errorMessage = String.format("Error Deleting user: %s ...", id);
            log.error(errorMessage, ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
