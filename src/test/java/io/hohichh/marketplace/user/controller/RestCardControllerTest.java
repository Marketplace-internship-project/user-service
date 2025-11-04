package io.hohichh.marketplace.user.controller;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestCardControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private RestCardController restCardController;

    private UUID testUserId;


    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    //=========== CARD TESTS ========================================================

    @Test
    void createCardForUser_ShouldReturnCreated_WithCardDto() {
        NewCardInfoDto newCardDto = new NewCardInfoDto("1234", "John Doe", LocalDate.now().plusYears(3));
        CardInfoDto cardDto = new CardInfoDto(UUID.randomUUID(), testUserId, "1234", "John Doe", LocalDate.now().plusYears(3));

        when(userService.createCardForUser(testUserId, newCardDto)).thenReturn(cardDto);

        ResponseEntity<CardInfoDto> response = restCardController.createCardForUser(testUserId, newCardDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(cardDto);
        verify(userService).createCardForUser(testUserId, newCardDto);
    }

    @Test
    void getCardByNumber_ShouldReturnNotFound_WhenCardNotFound() {
        String cardNumber = "9999";
        when(userService.getCardByNumber(cardNumber)).thenReturn(Optional.empty());

        ResponseEntity<CardInfoDto> response = restCardController.getCardByNumber(cardNumber);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).getCardByNumber(cardNumber);
    }

    @Test
    void getCardsByUserId_ShouldReturnOk_WithCardList() {
        CardInfoDto cardDto = new CardInfoDto(UUID.randomUUID(), testUserId, "1234", "John Doe", LocalDate.now().plusYears(3));
        List<CardInfoDto> cardList = List.of(cardDto);
        when(userService.getCardsByUserId(testUserId)).thenReturn(cardList);

        ResponseEntity<List<CardInfoDto>> response = restCardController.getCardsByUserId(testUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(cardList);
        verify(userService).getCardsByUserId(testUserId);
    }
}
