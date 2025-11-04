/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement Hibernate entities corresponding to data schema
 * User Entity --- users table
 */

package io.hohichh.marketplace.user.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a user of the marketplace.
 * <p>
 * This entity is mapped to the "users" table in the database.
 * It contains core user information such as name, email, and birth date.
 * It has a one-to-many relationship with {@link CardInfo} (managed from the CardInfo side).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {

    /**
     * The unique identifier for the user.
     * Generated automatically as a UUID.
     * The setter is private (AccessLevel.NONE) as the ID is managed by the persistence provider.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @Column(name = "id")
    private UUID id;

    /**
     * The user's first name.
     * This field is mandatory.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The user's last name (surname).
     * This field is optional.
     */
    @Column(name = "surname")
    private String surname;

    /**
     * The user's date of birth.
     * This field is optional.
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * The user's email address.
     * This field is mandatory and must be unique across all users.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
