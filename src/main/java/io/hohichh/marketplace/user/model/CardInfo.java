/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement Hibernate entities corresponding to data schema
 * CardInfo Entity --- card_info table
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
 * JPA entity representing a user's payment card information.
 * <p>
 * This entity is mapped to the "card_info" table in the database.
 * It holds details such as the card number, holder's name, and expiration date.
 * It also maintains a many-to-one relationship with the {@link User} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "card_info")
public class CardInfo {

    /**
     * The unique identifier for the card information.
     * Generated automatically as a UUID.
     * The setter is private (AccessLevel.NONE) as the ID is managed by the persistence provider.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @Setter(AccessLevel.NONE)
    private UUID id;

    /**
     * The user who owns this card.
     * This establishes a many-to-one relationship with the {@link User} entity.
     * The connection is lazy-loaded, and cascading operations (like persist, merge)
     * are applied to the associated User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The payment card number.
     * This field is mandatory and must be unique across all cards.
     */
    @Column(name = "number", nullable = false, unique = true)
    private String number;

    /**
     * The name of the cardholder, as it appears on the card.
     * This field is mandatory.
     */
    @Column(name = "holder", nullable = false)
    private String holder;

    /**
     * The expiration date of the card.
     * This field is mandatory.
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expiryDate;
}