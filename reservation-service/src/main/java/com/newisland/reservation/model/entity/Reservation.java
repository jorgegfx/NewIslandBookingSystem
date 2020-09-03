package com.newisland.reservation.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
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
public class Reservation {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(length = 36)
    @EqualsAndHashCode.Include
    private UUID id;

    @Type(type = "uuid-char")
    @Column(length = 36, nullable = false)
    private UUID userId;

    @Type(type = "uuid-char")
    @Column(length = 36, nullable = false)
    private UUID campsiteId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Basic
    @Column(nullable = false)
    private Instant arrivalDate;

    @Basic
    @Column(nullable = false)
    private Instant departureDate;

    @Basic
    @Column(nullable = false)
    private Instant createdOn;

    @Basic
    private Instant updatedOn;
}
