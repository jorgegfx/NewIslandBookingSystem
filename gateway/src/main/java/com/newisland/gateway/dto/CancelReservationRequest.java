package com.newisland.gateway.dto;

import com.newisland.common.messages.command.ReservationCommandOuterClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.newisland.common.messages.command.ReservationCommandOuterClass.ReservationCommand.ActionType.CANCEL;

@Data
@EqualsAndHashCode(callSuper=false)
public class CancelReservationRequest extends ReservationRequest{
    private String id;
    private String campsiteId;
    public CancelReservationRequest(){
        this.type = RequestType.CANCEL;
    }

    public ReservationCommandOuterClass.ReservationCommand toProtobuf(String correlationId){
        ReservationCommandOuterClass.CancelReservationCommand cancel =
                ReservationCommandOuterClass.CancelReservationCommand.newBuilder().
                        setId(id).
                        setCampsiteId(campsiteId).build();
        return ReservationCommandOuterClass.ReservationCommand.newBuilder().
                setActionType(CANCEL).
                setCorrelationId(correlationId).setCancel(cancel).build();
    }
}
