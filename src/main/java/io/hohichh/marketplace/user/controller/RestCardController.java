package io.hohichh.marketplace.user.controller;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/v1")
public class RestCardController {
    private final UserService userService;
    private final static Logger logger = LoggerFactory.getLogger(RestUserController.class);

    /**
     * Constructs a new RestUserController with the necessary UserService.
     *
     * @param userService The service layer responsible for user and card business logic.
     */
    public RestCardController(UserService userService){
        this.userService = userService;

        logger.trace("RestUserController initialized succesfully: userService has been injected");
    }

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
        logger.debug("Received request to create card for user with id: {}", userId);

        CardInfoDto newCard = userService.createCardForUser(userId, newCardDto);

        logger.info("Card created successfully with id: {} for user id: {}", newCard.id(), userId);
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
        logger.debug("Received request to delete card with id: {}", cardId);

        userService.deleteCard(cardId);

        logger.info("Card with id: {} deleted successfully", cardId);
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
        logger.debug("Received request to get card with id: {}", cardId);

        CardInfoDto card = userService.getCardById(cardId);

        logger.info("Card with id: {} retrieved successfully", cardId);
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
        logger.debug("Received request to get card with number: {}", cardNumber);

        Optional<CardInfoDto> cardOpt = userService.getCardByNumber(cardNumber);

        logger.debug("Search for card with number: {} {}", cardNumber,
                cardOpt.isPresent() ? "succeeded" : "failed - no cards with such number");
        logger.info("Get card by number request processed successfully");
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
        logger.debug("Received request to get cards for user with id: {}", userId);

        List<CardInfoDto> cards = userService.getCardsByUserId(userId);

        logger.info("Retrieved {} cards for user with id: {}", cards.size(), userId);
        return ResponseEntity.ok(cards);
    }


    /**
     * Retrieves a list of all payment cards that are expired.
     *
     * @return ResponseEntity containing a List of expired CardInfoDto and HTTP status 200 (OK).
     */
    @GetMapping(value = "/cards", params = "expiration-date=today")
    public ResponseEntity<List<CardInfoDto>> getExpiredCards() {
        logger.debug("Received request to get expired cards");

        List<CardInfoDto> cards = userService.getExpiredCards();

        logger.info("Found {} expired cards", cards.size());
        return ResponseEntity.ok(cards);
    }
}
