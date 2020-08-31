package com.newisland.user.dto;

import com.newisland.user.model.entity.User;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String email;

    public User toDomain(){
        return User.builder().email(name).name(name).build();
    }
}
