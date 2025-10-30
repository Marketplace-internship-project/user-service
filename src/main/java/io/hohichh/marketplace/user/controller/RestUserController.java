/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement the REST controller layer for the User Service,
 * providing API endpoints for managing users and their payment cards.
 */
package io.hohichh.marketplace.user.controller;

import io.hohichh.marketplace.user.dto.*;
import io.hohichh.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing users and their payment cards.
 * Provides API endpoints for CRUD operations on users and cards.
 * All endpoints are mapped under the base path "/api/v1".
 */
@RestController
@RequestMapping("/api/v1")
public class RestUserController {

    private final UserService userService;

    /**
     * Constructs a new RestUserController with the necessary UserService.
     *
     * @param userService The service layer responsible for user and card business logic.
     */
    public RestUserController(UserService userService){
        this.userService = userService;
    }

    //=========== USER METHODS ========================================================


    /**
     * Creates a new user.
     *
     * @param newUserDto DTO containing information for the new user. Must be valid.
     * @return ResponseEntity containing the created UserDto and HTTP status 201 (Created).
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        UserDto createdUser = userService.createUser(newUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    /**
     * Updates an existing user by their ID.
     *
     * @param id The UUID of the user to update.
     * @param userDto DTO containing the updated user information. Must be valid.
     * @return ResponseEntity containing the updated UserDto and HTTP status 200 (OK).
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody NewUserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }


    /**
     * Deletes a user by their ID.
     *
     * @param id The UUID of the user to delete.
     * @return ResponseEntity with HTTP status 204 (No Content).
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Retrieves a specific user by their ID, including their associated card information.
     *
     * @param id The UUID of the user to retrieve.
     * @return ResponseEntity containing the UserWithCardsDto and HTTP status 200 (OK).
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserWithCardsDto> getUserById(@PathVariable UUID id) {
        UserWithCardsDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    /**
     * Retrieves a specific user by their email address, including card information.
     *
     * @param email The email address of the user to retrieve.
     * @return ResponseEntity containing the UserWithCardsDto and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no user matches the email.
     */
    @GetMapping(value = "/users", params = "email")
    public ResponseEntity<UserWithCardsDto> getUserByEmail(@RequestParam String email) {
        Optional<UserWithCardsDto> userOpt = userService.getUserByEmail(email);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Retrieves a paginated list of all users or searches users based on a search term.
     * If a 'search' parameter is provided, it filters users based on the term (e.g., by name, email).
     * Otherwise, it returns a paginated list of all users.
     *
     * @param searchTerm Optional search term to filter users.
     * @param pageable Pagination information (page number, size, sort).
     * @return ResponseEntity containing a Page of UserDto and HTTP status 200 (OK).
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsersOrSearch(
            @RequestParam(name = "search", required = false) String searchTerm,
            Pageable pageable) {

        if (searchTerm != null && !searchTerm.isBlank()) {
            // Режим поиска
            Page<UserDto> users = userService.getUsersBySearchTerm(searchTerm, pageable);
            return ResponseEntity.ok(users);
        } else {
            // Режим "получить всех"
            Page<UserDto> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        }
    }


    /**
     * Retrieves a list of all users who have their birthday today.
     *
     * @return ResponseEntity containing a List of UserDto and HTTP status 200 (OK).
     */
    @GetMapping("/users/birthdays")
    public ResponseEntity<List<UserDto>> getUsersWithBirthdayToday() {
        List<UserDto> users = userService.getUsersWithBirthdayToday();
        return ResponseEntity.ok(users);
    }


    //=========== CARD METHODS ========================================================


    /**
     * Creates a new payment card and associates it with a specific user.
     *
     * @param userId The UUID of the user for whom the card is being created.
     * @param newCardDto DTO containing the new card's information. Must be valid.
     * @return ResponseEntity containing the created CardInfoDto and HTTP status 201 (Created).
     */
    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardInfoDto> createCardForUser(
            @PathVariable UUID userId,
            @Valid @RequestBody NewCardInfoDto newCardDto) {

        CardInfoDto newCard = userService.createCardForUser(userId, newCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
    }


    /**
     * Deletes a payment card by its ID.
     *
     * @param cardId The UUID of the card to delete.
     * @return ResponseEntity with HTTP status 204 (No Content).
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        userService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    /**
     * Retrieves a specific payment card by its ID.
     *
     * @param cardId The UUID of the card to retrieve.
     * @return ResponseEntity containing the CardInfoDto and HTTP status 200 (OK).
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CardInfoDto> getCardById(@PathVariable UUID cardId) {
        CardInfoDto card = userService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }


    /**
     * Retrieves a specific payment card by its card number.
     *
     * @param cardNumber The card number to search for.
     * @return ResponseEntity containing the CardInfoDto and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if no card matches the number.
     */
    @GetMapping(value = "/cards", params = "number")
    public ResponseEntity<CardInfoDto> getCardByNumber(@RequestParam("number") String cardNumber) {
        Optional<CardInfoDto> cardOpt = userService.getCardByNumber(cardNumber);
        return cardOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Retrieves all payment cards associated with a specific user.
     *
     * @param userId The UUID of the user whose cards are to be retrieved.
     * @return ResponseEntity containing a List of CardInfoDto and HTTP status 200 (OK).
     */
    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<CardInfoDto>> getCardsByUserId(@PathVariable UUID userId) {
        List<CardInfoDto> cards = userService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }


    /**
     * Retrieves a list of all payment cards that are expired.
     *
     * @return ResponseEntity containing a List of expired CardInfoDto and HTTP status 200 (OK).
     */
    @GetMapping("/cards/expired")
    public ResponseEntity<List<CardInfoDto>> getExpiredCards() {
        List<CardInfoDto> cards = userService.getExpiredCards();
        return ResponseEntity.ok(cards);
    }
}