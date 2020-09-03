package com.newisland.user.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(length = 36)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotEmpty(message = "Name is required!")
    @Column(nullable = false, length = 100)
    private String name;

    @NotEmpty(message = "Email is required!")
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Basic
    @Column(nullable = false)
    private Instant createdOn;

    @Basic
    private Instant updatedOn;

}
