package com.newisland.gateway.dto;

import lombok.Data;

import java.time.ZonedDateTime;
@Data
public class UpdateReservationDto {
    private String campsiteId;
    private ZonedDateTime arrivalDate;
    private ZonedDateTime departureDate;
}
