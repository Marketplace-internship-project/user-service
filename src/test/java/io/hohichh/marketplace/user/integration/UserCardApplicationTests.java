package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.exception.GlobalExceptionHandler;
import io.hohichh.marketplace.user.model.CardInfo;
import io.hohichh.marketplace.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserCardApplicationTests extends AbstractApplicationTest {
    private NewUserDto testUserDto;
    private NewCardInfoDto testCardDto;


    private static final String KNOWN_USER_ID_STRING = "123e4567-e89b-12d3-a456-426614174000";
    private static final UUID KNOWN_USER_ID = UUID.fromString(KNOWN_USER_ID_STRING);
    private static final String KNOWN_CARD_ID_STRING = "123e4567-e89b-12d3-a456-426614174001";
    private static final UUID KNOWN_CARD_ID = UUID.fromString(KNOWN_CARD_ID_STRING);


    private User mockUser;
    private CardInfo mockCard;

    @BeforeEach
    void initTestUserData(){
        testUserDto = new NewUserDto(
                "Adam",
                "FirstHuman",
                LocalDate.of(1999, 1,1),
                "AdamHuman@Gmail.com"
        );

        testCardDto = new NewCardInfoDto(
                "1111-1111-1111-1111",
                "ADAM FIRSTHUMAN",
                LocalDate.of(2026, 1,1 )
        );

        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(KNOWN_USER_ID);
        when(mockUser.getEmail()).thenReturn("adam@test.com");

        mockCard = mock(CardInfo.class);
        when(mockCard.getId()).thenReturn(KNOWN_CARD_ID);
        when(mockCard.getUser()).thenReturn(mockUser);
        when(mockCard.getNumber()).thenReturn(testCardDto.cardNumber());
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


    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void createCardForUser_shouldReturnCreatedCard() {
        doReturn(Optional.of(mockUser)).when(userRepository).findById(KNOWN_USER_ID);
        doReturn(Optional.empty()).when(cardRepository).findByNumber(testCardDto.cardNumber());

        doAnswer(invocation -> {
            CardInfo cardToSave = invocation.getArgument(0);
            return new CardInfoDto(KNOWN_CARD_ID, KNOWN_USER_ID, cardToSave.getNumber(), cardToSave.getHolder(), cardToSave.getExpiryDate());
        }).when(cardRepository).save(any(CardInfo.class));


        when(cardInfoMapper.toCardInfoDto(any(CardInfo.class))).thenAnswer(invocation -> {
            CardInfo c = invocation.getArgument(0);
            return new CardInfoDto(KNOWN_CARD_ID, c.getUser().getId(), c.getNumber(), c.getHolder(), c.getExpiryDate());
        });
        when(cardInfoMapper.toCardInfo(any(NewCardInfoDto.class))).thenAnswer(invocation -> {
            NewCardInfoDto dto = invocation.getArgument(0);
            CardInfo c = new CardInfo();
            c.setNumber(dto.cardNumber());
            c.setHolder(dto.cardHolderName());
            c.setExpiryDate(dto.expirationDate());
            return c;
        });

        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";
        ResponseEntity<CardInfoDto> response = restTemplate.postForEntity(
                url,
                testCardDto,
                CardInfoDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(KNOWN_USER_ID);
        assertThat(response.getBody().cardNumber()).isEqualTo(testCardDto.cardNumber());
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void createCardForUser_shouldReturnNotFound_whenUserDoesNotExist() {
        doReturn(Optional.empty()).when(userRepository).findById(KNOWN_USER_ID);

        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.postForEntity(
                url,
                testCardDto,
                GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void createCardForUser_shouldReturnConflict_whenCardNumberExists() {
        doReturn(Optional.of(mockUser)).when(userRepository).findById(KNOWN_USER_ID);
        doReturn(Optional.of(mockCard)).when(cardRepository).findByNumber(testCardDto.cardNumber());

        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.postForEntity(
                url,
                testCardDto,
                GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void createCardForUser_shouldReturnBadRequest_whenCardDataIsInvalid() {
        NewCardInfoDto invalidCard = new NewCardInfoDto(null, "Holder", LocalDate.now());
        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url,
                invalidCard,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void deleteCard_shouldReturnNoContent_whenCardExists() {
        doReturn(Optional.of(mockCard)).when(cardRepository).findById(KNOWN_CARD_ID);
        doReturn(true).when(cardRepository).existsById(KNOWN_CARD_ID);

        String url = "/v1/cards/" + KNOWN_CARD_ID;
        ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, null, Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void deleteCard_shouldReturnNotFound_whenCardDoesNotExist() {
        doReturn(Optional.empty()).when(cardRepository).findById(KNOWN_CARD_ID);
        doReturn(false).when(cardRepository).existsById(KNOWN_CARD_ID);

        String url = "/v1/cards/" + KNOWN_CARD_ID;
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.exchange(
                url, HttpMethod.DELETE, null, GlobalExceptionHandler.ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void getCardById_shouldReturnCard_whenCardExists() {
        doReturn(Optional.of(mockCard)).when(cardRepository).findById(KNOWN_CARD_ID);
        when(cardInfoMapper.toCardInfoDto(any(CardInfo.class))).thenReturn(
                new CardInfoDto(KNOWN_CARD_ID, KNOWN_USER_ID, testCardDto.cardNumber(), testCardDto.cardHolderName(), testCardDto.expirationDate())
        );

        String url = "/v1/cards/" + KNOWN_CARD_ID;
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(KNOWN_CARD_ID);
        assertThat(response.getBody().userId()).isEqualTo(KNOWN_USER_ID);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void getCardsByUserId_shouldReturnCardList() {
        doReturn(List.of(mockCard)).when(cardRepository).findByUserId(KNOWN_USER_ID);
        when(cardInfoMapper.toCardInfoDtoList(any(List.class))).thenReturn(
                List.of(new CardInfoDto(KNOWN_CARD_ID, KNOWN_USER_ID, testCardDto.cardNumber(), testCardDto.cardHolderName(), testCardDto.expirationDate()))
        );

        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).userId()).isEqualTo(KNOWN_USER_ID);
    }

    @Test
    @WithMockUser(username = KNOWN_USER_ID_STRING, roles = "USER")
    void getCardsByUserId_shouldReturnEmptyList_whenUserHasNoCards() {
        doReturn(List.of()).when(cardRepository).findByUserId(KNOWN_USER_ID);

        String url = "/v1/users/" + KNOWN_USER_ID + "/cards";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardByNumber_shouldReturnCard_whenCardExists() {
        doReturn(Optional.of(mockCard)).when(cardRepository).findByNumber(testCardDto.cardNumber());
        when(cardInfoMapper.toCardInfoDto(any(CardInfo.class))).thenReturn(
                new CardInfoDto(KNOWN_CARD_ID, KNOWN_USER_ID, testCardDto.cardNumber(), testCardDto.cardHolderName(), testCardDto.expirationDate())
        );

        String url = "/v1/cards?number={number}";
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class, testCardDto.cardNumber()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(KNOWN_CARD_ID);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardByNumber_shouldReturnNotFound_whenCardDoesNotExist() {
        doReturn(Optional.empty()).when(cardRepository).findByNumber(anyString());

        String url = "/v1/cards?number={number}";
        ResponseEntity<CardInfoDto> response = restTemplate.getForEntity(
                url, CardInfoDto.class, "0000-0000-0000-0000"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void getExpiredCards_shouldReturnOnlyExpiredCards() {
        final LocalDate MOCKED_TODAY = LocalDate.of(2025, 1, 1);
        Instant fixedInstant = MOCKED_TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        CardInfo expiredCard = mock(CardInfo.class);
        when(expiredCard.getId()).thenReturn(UUID.randomUUID());
        when(expiredCard.getNumber()).thenReturn("1111-EXPIRED");

        doReturn(List.of(expiredCard)).when(cardRepository).findExpiredCardsNative(MOCKED_TODAY);

        when(cardInfoMapper.toCardInfoDtoList(any(List.class))).thenReturn(
                List.of(new CardInfoDto(expiredCard.getId(), UUID.randomUUID(), expiredCard.getNumber(), "Expired", MOCKED_TODAY.minusDays(1)))
        );

        String url = "/v1/cards?expiration-date=today";
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<CardInfoDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, responseType
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).cardNumber()).isEqualTo("1111-EXPIRED");
    }
}