/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement service layer for user and card management
 */
package io.hohichh.marketplace.user.service;

import io.hohichh.marketplace.user.dto.*;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing users and their associated payment cards.
 * Defines the business logic operations for user and card entities.
 */
public interface UserService {

    /**
     * Creates a new user based on the provided data.
     *
     * @param user DTO containing information for the new user.
     * @return The created UserDto.
     */
    UserDto createUser(NewUserDto user);

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id The UUID of the user to delete.
     */
    void deleteUser(UUID id);

    /**
     * Updates an existing user with the provided data.
     *
     * @param id   The UUID of the user to update.
     * @param user DTO containing the updated user information.
     * @return The updated UserDto.
     */
    UserDto updateUser(UUID id, NewUserDto user);

    /**
     * Retrieves a user and their associated cards by the user's unique identifier.
     *
     * @param id The UUID of the user to retrieve.
     * @return The UserWithCardsDto containing user and card details.
     * @throws NotFoundException if the user cannot be found.
     */
    UserWithCardsDto getUserById(UUID id);

    /**
     * Finds a user and their associated cards by their email address.
     *
     * @param email The email address to search for.
     * @return An Optional containing the UserWithCardsDto if found, or an empty Optional otherwise.
     */
    Optional<UserWithCardsDto> getUserByEmail(String email);

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable Pagination and sorting information.
     * @return A Page containing UserDto objects.
     */
    Page<UserDto> getAllUsers(Pageable pageable);

    /**
     * Finds all users whose birthday is today.
     *
     * @return A List of UserDto objects for users with a birthday today.
     */
    List<UserDto> getUsersWithBirthdayToday();

    /**
     * Searches for users based on a provided search term (e.g., name, email).
     *
     * @param searchTerm The term to search for.
     * @param pageable   Pagination and sorting information.
     * @return A Page of UserDto objects matching the search term.
     */
    Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Creates and associates a new payment card with a specific user.
     *
     * @param userId   The UUID of the user to whom the card will be added.
     * @param cardInfo DTO containing the new card details.
     * @return The created CardInfoDto.
     * @throws NotFoundException if the user cannot be found.
     */
    CardInfoDto createCardForUser(UUID userId, NewCardInfoDto cardInfo);

    /**
     * Deletes a payment card by its unique identifier.
     *
     * @param cardId The UUID of the card to delete.
     */
    void deleteCard(UUID cardId);

    /**
     * Retrieves a payment card by its unique identifier.
     *
     * @param cardId The UUID of the card to retrieve.
     * @return The CardInfoDto.
     * @throws NotFoundException if the card cannot be found.
     */
    CardInfoDto getCardById(UUID cardId);

    /**
     * Finds a payment card by its card number.
     *
     * @param cardNumber The card number to search for.
     * @return An Optional containing the CardInfoDto if found, or an empty Optional otherwise.
     */
    Optional<CardInfoDto> getCardByNumber(String cardNumber);

    /**
     * Retrieves all payment cards associated with a specific user.
     *
     * @param userId The UUID of the user.
     * @return A List of CardInfoDto objects belonging to the user.
     * @throws NotFoundException if the user cannot be found.
     */
    List<CardInfoDto> getCardsByUserId(UUID userId);

    /**
     * Retrieves a list of all payment cards that are currently expired.
     *
     * @return A List of expired CardInfoDto objects.
     */
    List<CardInfoDto> getExpiredCards();
}