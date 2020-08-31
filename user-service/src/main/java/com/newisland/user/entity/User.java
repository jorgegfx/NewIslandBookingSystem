package com.newisland.user.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    @NotEmpty(message = "Name is required!")
    @Column(nullable = false, length = 100)
    private String name;

    @NotEmpty(message = "Email is required!")
    @Column(nullable = false, length = 100)
    private String email;

    @Basic
    @Column(nullable = false)
    private Instant createdOn;

    @Basic
    @Column(nullable = true)
    private Instant updateOn;

}