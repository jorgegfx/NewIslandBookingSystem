package com.newisland.gateway.dto;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.CREATE;

@Data
@EqualsAndHashCode(callSuper=false)
public class CreateReservationRequest extends ReservationRequest{
    private String userFullName;
    private String userEmail;
    private String campsiteId;
    private ZonedDateTime arrivalDate;
    private ZonedDateTime departureDate;

    public CreateReservationRequest(){
        this.type = RequestType.CREATE;
    }

    public ReservationCommandOuterClass.ReservationCommand toProtobuf(UUID correlationId){
        ReservationCommandOuterClass.CreateReservationCommand create =
                ReservationCommandOuterClass.CreateReservationCommand.newBuilder().
                        setCampsiteId(this.getCampsiteId()).
                        setUserEmail(this.getUserEmail()).
                        setUserFullName(this.getUserFullName()).
                        setArrivalDate(TimeUtils.convertTimestamp(this.getArrivalDate())).
                        setDepartureDate(TimeUtils.convertTimestamp(this.getDepartureDate())).
                        build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setCorrelationId(correlationId.toString()).
                        setActionType(CREATE).setCreate(create).build();
        return cmd;
    }
}
