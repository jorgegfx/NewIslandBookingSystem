package com.newisland.reservation.model.entity;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReservationTransaction {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Type(type = "uuid-char")
    @Column(length = 36, nullable = false)
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    private ReservationTransactionType type;

    @Enumerated(EnumType.STRING)
    private ReservationTransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column( length = 255)
    private String errorMessage;

    @Basic
    @Column(nullable = false)
    private Instant createdOn;
}
