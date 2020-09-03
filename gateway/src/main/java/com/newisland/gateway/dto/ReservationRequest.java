package com.newisland.gateway.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateReservationRequest.class, name = "create"),
        @JsonSubTypes.Type(value = UpdateReservationRequest.class, name = "update")
})
public abstract class ReservationRequest {

    protected RequestType type;

    public RequestType getType() {
        return type;
    }
}
