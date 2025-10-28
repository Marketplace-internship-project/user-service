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

@RestController
@RequestMapping("/api/v1")
public class RestUserController {

    private final UserService userService;

    public RestUserController(UserService userService){
        this.userService = userService;
    }

    //=========== USER METHODS ========================================================


    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody NewUserDto newUserDto) {
        UserDto createdUser = userService.createUser(newUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody NewUserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/users/{id}")
    public ResponseEntity<UserWithCardsDto> getUserById(@PathVariable UUID id) {
        UserWithCardsDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping(value = "/users", params = "email")
    public ResponseEntity<UserWithCardsDto> getUserByEmail(@RequestParam String email) {
        Optional<UserWithCardsDto> userOpt = userService.getUserByEmail(email);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


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
     * Получить пользователей, у которых сегодня день рождения.
     * Возвращает 200 OK и пустой список, если таких нет.
     */
    @GetMapping("/users/birthdays")
    public ResponseEntity<List<UserDto>> getUsersWithBirthdayToday() {
        List<UserDto> users = userService.getUsersWithBirthdayToday();
        return ResponseEntity.ok(users);
    }


    //=========== CARD METHODS ========================================================


    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardInfoDto> createCardForUser(
            @PathVariable UUID userId,
            @Valid @RequestBody NewCardInfoDto newCardDto) {

        CardInfoDto newCard = userService.createCardForUser(userId, newCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
    }


    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        userService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CardInfoDto> getCardById(@PathVariable UUID cardId) {
        CardInfoDto card = userService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }


    @GetMapping(value = "/cards", params = "number")
    public ResponseEntity<CardInfoDto> getCardByNumber(@RequestParam("number") String cardNumber) {
        Optional<CardInfoDto> cardOpt = userService.getCardByNumber(cardNumber);
        return cardOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<CardInfoDto>> getCardsByUserId(@PathVariable UUID userId) {
        List<CardInfoDto> cards = userService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }


    @GetMapping("/cards/expired")
    public ResponseEntity<List<CardInfoDto>> getExpiredCards() {
        List<CardInfoDto> cards = userService.getExpiredCards();
        return ResponseEntity.ok(cards);
    }
}