package com.newisland.reservation.client;

import java.util.UUID;

public interface UserServiceClient {
    UUID createUser(String email, String name);
}
