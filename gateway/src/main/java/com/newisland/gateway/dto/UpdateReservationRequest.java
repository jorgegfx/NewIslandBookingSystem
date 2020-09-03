package com.newisland.gateway.dto;

import com.newisland.common.dto.utils.TimeUtils;
import com.newisland.common.messages.command.ReservationCommandOuterClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.UUID;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.UPDATE;

@Data
@EqualsAndHashCode(callSuper=false)
public class UpdateReservationRequest extends ReservationRequest{
    private String id;
    private String campsiteId;
    private ZonedDateTime arrivalDate;
    private ZonedDateTime departureDate;

    public UpdateReservationRequest() {
        this.type = RequestType.UPDATE;
    }

    public ReservationCommandOuterClass.ReservationCommand toProtobuf(String id){
        ReservationCommandOuterClass.UpdateReservationCommand update =
                ReservationCommandOuterClass.UpdateReservationCommand.newBuilder().
                        setId(id).
                        setCampsiteId(this.getCampsiteId()).
                        setArrivalDate(TimeUtils.convertTimestamp(this.getArrivalDate())).
                        setDepartureDate(TimeUtils.convertTimestamp(this.getDepartureDate())).
                        build();
        ReservationCommandOuterClass.ReservationCommand cmd =
                ReservationCommandOuterClass.ReservationCommand.newBuilder().
                        setActionType(UPDATE).setUpdate(update).build();
        return cmd;
    }

    public ReservationCommandOuterClass.ReservationCommand toProtobuf(){
        return toProtobuf(this.id);
    }
}
