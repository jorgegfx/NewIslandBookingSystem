package com.newisland.reservation.client;

import com.newisland.user.dto.CreateUserRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class UserServiceClientImpl implements UserServiceClient{

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user-service-url}")
    private String userServiceUrl;

    @Data
    class UserResponse{
        private UUID id;
        private String name;
        private String email;
    }

    @Override
    public UUID createUser(String email, String name) {
        try {
            HttpEntity<CreateUserRequest> request =
                    new HttpEntity<>(CreateUserRequest.builder().email(email).name(name).build());
            UserResponse userResponse = restTemplate.postForObject(userServiceUrl,request,UserResponse.class);
            return userResponse.getId();
        }catch (Exception ex){
            throw new IllegalStateException(ex);
        }
    }
}
