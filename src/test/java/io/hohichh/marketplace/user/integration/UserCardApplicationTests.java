package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.exception.GlobalExceptionHandler;
import io.hohichh.marketplace.user.repository.CardRepository;
import io.hohichh.marketplace.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserCardApplicationTests extends AbstractApplicationTest {
    private NewUserDto testUser;
    private NewCardInfoDto testCard;

    @BeforeEach
    void initTestUserData(){
        testUser = new NewUserDto(
                "Adam",
                "FirstHuman",
                LocalDate.of(1999, 1,1),
                "AdamHuman@Gmail.com"
        );

        testCard = new NewCardInfoDto(
                "1111-1111-1111-1111",
                "ADAM FIRSTHUMAN",
                LocalDate.of(2026, 1,1 )
        );
    }

    @AfterEach
    void tearDown() {
        //clear redis cache
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(java.util.Objects::nonNull)
                .forEach(org.springframework.cache.Cache::clear);

        //clear postgres database
        userRepository.deleteAll();
    }


    private UUID createTestUser(NewUserDto userToCreate) {
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                "/v1/users",
                userToCreate,
                UserDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().id();
    }


    private CardInfoDto createTestCard(UUID userId, NewCardInfoDto cardToCreate) {
        String url = "/v1/users/" + userId + "/cards";
        ResponseEntity<CardInfoDto> response = restTemplate.postForEntity(
                url,
                cardToCreate,
                CardInfoDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }


    @Test
    void createCardForUser_shouldReturnCreatedCard() {
        UUID userId = createTestUser(testUser);
        CardInfoDto createdCard = createTestCard(userId, testCard);

        assertThat(createdCard.userId()).isEqualTo(userId);
        assertThat(createdCard.cardNumber()).isEqualTo(testCard.cardNumber());
        assertThat(createdCard.cardHolderName()).isEqualTo(testCard.cardHolderName());
    }

    @Test
    void createCardForUser_shouldReturnNotFound_whenUserDoesNotExist() {
        UUID nonexistentUserId = UUID.randomUUID();
        String url = "/v1/users/" + nonexistentUserId + "/cards";

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.postForEntity(
                url,
                testCard,
                GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message())
                .isEqualTo("User with id " + nonexistentUserId + " not found.");
    }

    @Test
    void createCardForUser_shouldReturnConflict_whenCardNumberExists() {
        UUID userId1 = createTestUser(testUser);
        createTestCard(userId1, testCard);

        NewUserDto testUser2 = new NewUserDto("Eve", "Second", null, "eve@gmail.com");
        UUID userId2 = createTestUser(testUser2);

        String url = "/v1/users/" + userId2 + "/cards";

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.postForEntity(
                url,
                testCard,
                GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message())
                .isEqualTo("Card with number " + testCard.cardNumber() + " already exists.");
    }

    @Test
    void createCardForUser_shouldReturnBadRequest_whenCardDataIsInvalid() {
        UUID userId = createTestUser(testUser);

        NewCardInfoDto invalidCard = new NewCardInfoDto(
                null, "Holder", LocalDate.now()
        );
        String url = "/v1/users/" + userId + "/cards";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                invalidCard,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().toString()).contains("cardNumber");
    }

    @Test
    void deleteCard_shouldReturnNoContent_whenCardExists() {
        UUID userId = createTestUser(testUser);
        CardInfoDto createdCard = createTestCard(userId, testCard);

        String url = "/v1/cards/" + createdCard.id();
        ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, null, Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> getResponse = restTemplate.getForEntity(
                url, GlobalExceptionHandler.ErrorResponse.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteCard_shouldReturnNotFound_whenCardDoesNotExist() {
        UUID nonexistentCardId = UUID.randomUUID();
        String url = "/v1/cards/" + nonexistentCardId;

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.exchange(
                url, HttpMethod.DELETE, null, GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message())
                .isEqualTo("Card with id " + nonexistentCardId + " not found.");
    }

    @Test
    void getCardById_shouldReturnCard_whenCardExists() {
        UUID userId = createTestUser(testUser);
        CardInfoDto createdCard = createTestCard(userId, testCard);

        String url = "/v1/cards/" + createdCard.id();
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdCard.id());
        assertThat(response.getBody().cardNumber()).isEqualTo(testCard.cardNumber());
    }

    @Test
    void getCardByNumber_shouldReturnCard_whenCardExists() {
        UUID userId = createTestUser(testUser);
        CardInfoDto createdCard = createTestCard(userId, testCard);

        String url = "/v1/cards?number={number}";
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class, createdCard.cardNumber()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdCard.id());
    }

    @Test
    void getCardByNumber_shouldReturnNotFound_whenCardDoesNotExist() {
        String url = "/v1/cards?number={number}";
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class, "0000-0000-0000-0000"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getCardsByUserId_shouldReturnCardList() {
        UUID userId = createTestUser(testUser);

        CardInfoDto card1 = createTestCard(userId, testCard);

        NewCardInfoDto testCard2 = new NewCardInfoDto(
                "2222-2222-2222-2222",
                "ADAM FIRSTHUMAN",
                LocalDate.of(2027, 1, 1)
        );
        CardInfoDto card2 = createTestCard(userId, testCard2);

        String url = "/v1/users/" + userId + "/cards";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardInfoDto> cards = response.getBody();
        assertThat(cards).isNotNull();
        assertThat(cards).hasSize(2);
        assertThat(cards)
                .extracting(CardInfoDto::cardNumber)
                .containsExactlyInAnyOrder(card1.cardNumber(), card2.cardNumber());
    }

    @Test
    void getCardsByUserId_shouldReturnEmptyList_whenUserHasNoCards() {
        UUID userId = createTestUser(testUser);

        String url = "/v1/users/" + userId + "/cards";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void getExpiredCards_shouldReturnOnlyExpiredCards() {
        final LocalDate MOCKED_TODAY = LocalDate.of(2025, 1, 1);
        Instant fixedInstant = MOCKED_TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        UUID userId = createTestUser(testUser);

        NewCardInfoDto expiredCardDto = new NewCardInfoDto(
                "1111-EXPIRED", "Expired Card", MOCKED_TODAY.minusDays(1)
        );
        CardInfoDto expiredCard = createTestCard(userId, expiredCardDto);

        NewCardInfoDto activeCardDto = new NewCardInfoDto(
                "2222-ACTIVE", "Active Card", MOCKED_TODAY
        );
        createTestCard(userId, activeCardDto);

        String url = "/v1/cards/expired";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CardInfoDto> cards = response.getBody();
        assertThat(cards).isNotNull();
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).id()).isEqualTo(expiredCard.id());
        assertThat(cards.get(0).cardNumber()).isEqualTo("1111-EXPIRED");
    }
}