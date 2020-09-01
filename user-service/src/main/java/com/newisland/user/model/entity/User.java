package com.newisland.user.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false,unique = true)
    private String uuid;

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
    private Instant updateOn;

}
