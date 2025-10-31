package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class UserCachingApplicationTest extends AbstractApplicationTest {

    private NewUserDto testUserDto;
    private NewCardInfoDto testCardDto;
    private final LocalDate MOCKED_BIRTHDAY = LocalDate.of(1990, 10, 30);
    private final LocalDate OTHER_DAY = LocalDate.of(2000, 1, 1);

    @BeforeEach
    void setUp() {
        testUserDto = new NewUserDto(
                "Cache",
                "Tester",
                OTHER_DAY,
                "cache.tester@gmail.com"
        );

        testCardDto = new NewCardInfoDto(
                "1111-2222-3333-4444",
                "Cache Tester",
                LocalDate.of(2030, 1, 1) // Точно не просроченная
        );
    }


    private void setMockToday(LocalDate today) {
        Instant fixedInstant = today.atStartOfDay(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    private UserDto createTestUser(NewUserDto userDto) {
        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                "/v1/users",
                userDto,
                UserDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }


    private void updateTestUser(UUID userId, NewUserDto userDto) {
        HttpEntity<NewUserDto> requestEntity = new HttpEntity<>(userDto);
        restTemplate.exchange(
                "/v1/users/" + userId,
                HttpMethod.PUT,
                requestEntity,
                UserDto.class
        );
    }

    private void deleteTestUser(UUID userId) {
        restTemplate.delete("/v1/users/" + userId);
    }


    private void getUserById(UUID userId) {
        restTemplate.getForEntity(
                "/v1/users/" + userId,
                UserWithCardsDto.class
        );
    }

    private void getBirthdayUsers() {
        ParameterizedTypeReference<List<UserDto>> responseType = new ParameterizedTypeReference<>() {};
        restTemplate.exchange(
                "/v1/users/birthdays",
                HttpMethod.GET,
                null,
                responseType
        );
    }

    private CardInfoDto createTestCard(UUID userId, NewCardInfoDto cardDto) {
        String url = "/v1/users/" + userId + "/cards";
        ResponseEntity<CardInfoDto> response = restTemplate.postForEntity(
                url,
                cardDto,
                CardInfoDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void getExpiredCards() {
        ParameterizedTypeReference<List<CardInfoDto>> responseType = new ParameterizedTypeReference<>() {};
        restTemplate.exchange(
                "/v1/cards/expired",
                HttpMethod.GET,
                null,
                responseType
        );
    }


    @Test
    void getUserById_shouldCacheUserOnFirstCall() {
        UserDto createdUser = createTestUser(testUserDto);
        clearInvocations(userRepository);

        getUserById(createdUser.id());
        getUserById(createdUser.id());
        getUserById(createdUser.id());

        verify(userRepository, times(1)).findById(createdUser.id());
    }

    @Test
    void updateUser_shouldEvict_usersCache() {
        UserDto createdUser = createTestUser(testUserDto);

        getUserById(createdUser.id());
        clearInvocations(userRepository);

        updateTestUser(createdUser.id(), new NewUserDto("New", "Name", null, "new.email@gmail.com"));

        getUserById(createdUser.id());

        verify(userRepository, times(2)).findById(createdUser.id());
    }

    @Test
    void deleteUser_shouldEvict_usersCache() {
        UserDto createdUser = createTestUser(testUserDto);

        getUserById(createdUser.id());
        clearInvocations(userRepository);

        deleteTestUser(createdUser.id());

        restTemplate.getForEntity(
                "/v1/users/" + createdUser.id(),
                Object.class
        );

        verify(userRepository, times(1)).findById(createdUser.id());
    }



    @Test
    void getBirthdayUsers_shouldCacheResult() {
        setMockToday(MOCKED_BIRTHDAY);

        getBirthdayUsers();
        getBirthdayUsers();

        verify(userRepository, times(1)).findUsersWithBirthDayToday(MOCKED_BIRTHDAY);
    }

    @Test
    void createUser_shouldEvict_birthdayCache() {
        setMockToday(MOCKED_BIRTHDAY);

        getBirthdayUsers();
        clearInvocations(userRepository);

        createTestUser(testUserDto);

        getBirthdayUsers();

        verify(userRepository, times(1)).findUsersWithBirthDayToday(MOCKED_BIRTHDAY);
    }

    @Test
    void updateUser_shouldEvict_birthdayCache() {
        setMockToday(MOCKED_BIRTHDAY);
        UserDto createdUser = createTestUser(testUserDto);

        getBirthdayUsers();
        clearInvocations(userRepository);

        updateTestUser(createdUser.id(), new NewUserDto("New", "Name", null, "new.email@gmail.com"));

        getBirthdayUsers();

        verify(userRepository, times(1)).findUsersWithBirthDayToday(MOCKED_BIRTHDAY);
    }

    @Test
    void deleteUser_shouldEvict_birthdayCache() {
        setMockToday(MOCKED_BIRTHDAY);
        UserDto createdUser = createTestUser(testUserDto);

        getBirthdayUsers();
        clearInvocations(userRepository);

        deleteTestUser(createdUser.id());

        getBirthdayUsers();

        verify(userRepository, times(1)).findUsersWithBirthDayToday(MOCKED_BIRTHDAY);
    }

    @Test
    void createCardForUser_shouldEvict_usersCache() {
        UserDto createdUser = createTestUser(testUserDto);

        getUserById(createdUser.id());
        clearInvocations(userRepository);

        createTestCard(createdUser.id(), testCardDto);

        getUserById(createdUser.id());

        verify(userRepository, times(2)).findById(createdUser.id());
    }

    @Test
    void getExpiredCards_shouldCacheResult() {
        final LocalDate MOCKED_TODAY = LocalDate.of(2025, 1, 1);
        setMockToday(MOCKED_TODAY);

        getExpiredCards();
        getExpiredCards();

        verify(cardRepository, times(1)).findExpiredCardsNative(MOCKED_TODAY);
    }
}