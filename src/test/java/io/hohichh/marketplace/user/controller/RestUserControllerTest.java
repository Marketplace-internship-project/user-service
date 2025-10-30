package io.hohichh.marketplace.user.controller;

import io.hohichh.marketplace.user.dto.*;
import io.hohichh.marketplace.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestUserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private RestUserController restUserController;

    private UUID testUserId;
    private UserDto testUserDto;
    private NewUserDto testNewUserDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNewUserDto = new NewUserDto("John", "Doe", LocalDate.now().minusYears(20), "john@example.com");
        testUserDto = new UserDto(testUserId, "John", "Doe", LocalDate.now().minusYears(20), "john@example.com");
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WithCreatedStatus() {
        when(userService.createUser(any(NewUserDto.class))).thenReturn(testUserDto);

        ResponseEntity<UserDto> response = restUserController.createUser(testNewUserDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(testUserDto);

        verify(userService, times(1)).createUser(testNewUserDto);
    }

    @Test
    void updateUser_ShouldReturnOk_WithUpdatedUser() {
        when(userService.updateUser(eq(testUserId), any(NewUserDto.class))).thenReturn(testUserDto);

        ResponseEntity<UserDto> response = restUserController.updateUser(testUserId, testNewUserDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testUserDto);
        verify(userService).updateUser(testUserId, testNewUserDto);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        doNothing().when(userService).deleteUser(testUserId);

        ResponseEntity<Void> response = restUserController.deleteUser(testUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(testUserId);
    }

    @Test
    void getUserById_ShouldReturnOk_WithUser() {
        UserWithCardsDto testUserWithCards = new UserWithCardsDto(testUserId, "John", null, null, "john@example.com", Collections.emptyList());
        when(userService.getUserById(testUserId)).thenReturn(testUserWithCards);

        ResponseEntity<UserWithCardsDto> response = restUserController.getUserById(testUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testUserWithCards);
        verify(userService).getUserById(testUserId);
    }

    @Test
    void getUserByEmail_ShouldReturnOk_WhenUserFound() {
        String email = "john@example.com";
        UserWithCardsDto testUserWithCards = new UserWithCardsDto(testUserId, "John", null, null, email, Collections.emptyList());
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(testUserWithCards));

        ResponseEntity<UserWithCardsDto> response = restUserController.getUserByEmail(email);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testUserWithCards);
        verify(userService).getUserByEmail(email);
    }

    @Test
    void getUserByEmail_ShouldReturnNotFound_WhenUserNotFound() {
        String email = "notfound@example.com";
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<UserWithCardsDto> response = restUserController.getUserByEmail(email);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).getUserByEmail(email);
    }

    @Test
    void getAllUsersOrSearch_ShouldCallGetAllUsers_WhenSearchTermIsNull() {
        Pageable pageable = Pageable.unpaged();
        Page<UserDto> userPage = new PageImpl<>(List.of(testUserDto), pageable, 1);
        when(userService.getAllUsers(pageable)).thenReturn(userPage);

        ResponseEntity<Page<UserDto>> response = restUserController.getAllUsersOrSearch(null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userPage);
        verify(userService).getAllUsers(pageable);
        verify(userService, never()).getUsersBySearchTerm(any(), any());
    }

    @Test
    void getAllUsersOrSearch_ShouldCallGetUsersBySearchTerm_WhenSearchTermIsPresent() {
        String searchTerm = "John";
        Pageable pageable = Pageable.unpaged();
        Page<UserDto> userPage = new PageImpl<>(List.of(testUserDto), pageable, 1);
        when(userService.getUsersBySearchTerm(searchTerm, pageable)).thenReturn(userPage);

        ResponseEntity<Page<UserDto>> response = restUserController.getAllUsersOrSearch(searchTerm, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userPage);
        verify(userService, never()).getAllUsers(pageable);
        verify(userService).getUsersBySearchTerm(searchTerm, pageable);
    }

    @Test
    void getUsersWithBirthdayToday_ShouldReturnOk_WithUserList() {
        List<UserDto> birthdayUsers = List.of(testUserDto);
        when(userService.getUsersWithBirthdayToday()).thenReturn(birthdayUsers);

        ResponseEntity<List<UserDto>> response = restUserController.getUsersWithBirthdayToday();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(birthdayUsers);
        verify(userService).getUsersWithBirthdayToday();
    }

    //=========== CARD TESTS ========================================================

    @Test
    void createCardForUser_ShouldReturnCreated_WithCardDto() {
        NewCardInfoDto newCardDto = new NewCardInfoDto("1234", "John Doe", LocalDate.now().plusYears(3));
        CardInfoDto cardDto = new CardInfoDto(UUID.randomUUID(), testUserId, "1234", "John Doe", LocalDate.now().plusYears(3));

        when(userService.createCardForUser(testUserId, newCardDto)).thenReturn(cardDto);

        ResponseEntity<CardInfoDto> response = restUserController.createCardForUser(testUserId, newCardDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(cardDto);
        verify(userService).createCardForUser(testUserId, newCardDto);
    }

    @Test
    void getCardByNumber_ShouldReturnNotFound_WhenCardNotFound() {
        String cardNumber = "9999";
        when(userService.getCardByNumber(cardNumber)).thenReturn(Optional.empty());

        ResponseEntity<CardInfoDto> response = restUserController.getCardByNumber(cardNumber);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).getCardByNumber(cardNumber);
    }

    @Test
    void getCardsByUserId_ShouldReturnOk_WithCardList() {
        CardInfoDto cardDto = new CardInfoDto(UUID.randomUUID(), testUserId, "1234", "John Doe", LocalDate.now().plusYears(3));
        List<CardInfoDto> cardList = List.of(cardDto);
        when(userService.getCardsByUserId(testUserId)).thenReturn(cardList);

        ResponseEntity<List<CardInfoDto>> response = restUserController.getCardsByUserId(testUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(cardList);
        verify(userService).getCardsByUserId(testUserId);
    }
}