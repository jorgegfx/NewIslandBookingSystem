package com.newisland.gateway.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CreateReservationDto {
    private String userFullName;
    private String userEmail;
    private String campsiteId;
    private ZonedDateTime arrivalDate;
    private ZonedDateTime departureDate;
}
