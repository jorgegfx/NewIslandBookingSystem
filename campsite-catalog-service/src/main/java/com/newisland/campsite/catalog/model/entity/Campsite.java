package com.newisland.campsite.catalog.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Campsite {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @EqualsAndHashCode.Include
    @Type(type = "uuid-char")
    @Column(length = 36)
    private UUID id;

    @NotEmpty(message = "Name is required!")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    @Basic
    @Column(nullable = false)
    private Instant createdOn;

    @Basic
    private Instant updatedOn;
}
