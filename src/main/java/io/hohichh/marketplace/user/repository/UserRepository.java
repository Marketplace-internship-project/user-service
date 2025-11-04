/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement dao layer and basic CRUD operations
 * CardInfo DAO as JPA Repository
 */
package io.hohichh.marketplace.user.repository;

import io.hohichh.marketplace.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides standard CRUD operations and custom queries for managing users.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their unique email address.
     * <p>
     * This method is created for demonstration of query derivation from the method name.
     *
     * @param email The user's email address to search for.
     * @return An {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not.
     */
    Optional<User> findByEmail(String email);

    /**
     * Searches for users whose name, surname, or email contains the given search term (case-insensitive).
     * Results are returned in a paginated format.
     * <p>
     * This method is created for demonstration of a custom JPQL query.
     *
     * @param searchTerm The string to search for in the user's name, surname, or email.
     * @param pageable   Pagination and sorting information.
     * @return A {@link Page} of {@link User} entities matching the search term.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Finds all users whose birthday (month and day) matches the current date.
     * <p>
     * This method is created for demonstration of a native SQL query.
     *
     * @return A {@link List} of {@link User} entities whose birthday is today.
     */
    @Query(value = "SELECT id, name, surname, birth_date, email FROM users u " +
            "WHERE EXTRACT(MONTH FROM u.birth_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(DAY FROM u.birth_date) = EXTRACT(DAY FROM CURRENT_DATE)",
            nativeQuery = true)
    List<User> findUsersWithBirthDayTodayNative();
}