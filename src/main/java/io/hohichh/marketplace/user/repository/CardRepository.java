/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement dao layer and basic CRUD operations
 * CardInfo DAO as JPA Repository
 */

package io.hohichh.marketplace.user.repository;

import io.hohichh.marketplace.user.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link CardInfo} entities.
 * Provides standard CRUD operations and custom queries for managing payment cards.
 */
public interface CardRepository extends JpaRepository<CardInfo, UUID> {

    /**
     * Finds a card by its unique card number.
     * <p>
     * This method is created for demonstration of query derivation from the method name.
     *
     * @param number The card number to search for.
     * @return An {@link Optional} containing the {@link CardInfo} if found, or an empty {@link Optional} if not.
     */
    Optional<CardInfo> findByNumber(String number);

    /**
     * Finds all cards associated with a specific user ID.
     * <p>
     * This method is created for demonstration of a custom JPQL query.
     *
     * @param userId The UUID of the user.
     * @return A {@link List} of {@link CardInfo} entities associated with the given user.
     */
    @Query("SELECT c FROM CardInfo c WHERE c.user.id = :userId")
    List<CardInfo> findByUserId(UUID userId);

    /**
     * Finds all cards where the expiration date is before the current date.
     * <p>
     * This method is created for demonstration of a native SQL query.
     *
     * @return A {@link List} of expired {@link CardInfo} entities.
     */
    @Query(value = "SELECT id, user_id, \"number\", holder, expiration_date " +
            "FROM card_info " +
            "WHERE expiration_date < CAST(:date AS date)",
            nativeQuery = true)
    List<CardInfo> findExpiredCardsNative(@Param("date") LocalDate date);
}