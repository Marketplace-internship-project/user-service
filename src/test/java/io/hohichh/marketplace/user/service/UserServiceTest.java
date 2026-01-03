package io.hohichh.marketplace.user.service;

import io.hohichh.marketplace.user.dto.*;
import io.hohichh.marketplace.user.exception.ResourceCreationConflictException;
import io.hohichh.marketplace.user.exception.ResourceNotFoundException;
import io.hohichh.marketplace.user.mapper.CardInfoMapper;
import io.hohichh.marketplace.user.mapper.UserMapper;
import io.hohichh.marketplace.user.model.CardInfo;
import io.hohichh.marketplace.user.model.User;
import io.hohichh.marketplace.user.repository.CardRepository;
import io.hohichh.marketplace.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CardInfoMapper cardInfoMapper;

    @Mock
    private Clock clock;
    private final LocalDate frozenDate = LocalDate.of(2025, 1, 15);

    @InjectMocks
    private UserServiceImpl userService;


    //========================================================================
    //CREATE USER TEST
    @Test
    void createUser_shouldSaveAndReturnUserDto() {

        NewUserDto newUserDto = new NewUserDto("John", "Doe", null, "john.doe@example.com");


        User userEntity = new User();
        User savedUserEntity = new User();
        UserDto expectedUserDto = new UserDto(UUID.randomUUID(), "John", "Doe", null, "john.doe@example.com");


        when(userMapper.toUser(any(NewUserDto.class))).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(savedUserEntity);
        when(userMapper.toUserDto(any(User.class))).thenReturn(expectedUserDto);

        UserDto result = userService.createUser(newUserDto);

        assertNotNull(result);
        assertEquals(expectedUserDto.email(), result.email());

        verify(userMapper).toUser(newUserDto);
        verify(userRepository).save(userEntity);
        verify(userMapper).toUserDto(savedUserEntity);
    }

    @Test
    void createUser_shouldThrowResourceCreationConflictException_whenUserWithEmailExists() {
        NewUserDto newUserDto = new NewUserDto("John", "Doe",
                null, "john.doe@example.com");

        when(userRepository.findByEmail(newUserDto.email()))
                .thenReturn(Optional.of(new User()));

        assertThrows(ResourceCreationConflictException.class, () ->
            userService.createUser(newUserDto)
        );

        verify(userRepository).findByEmail(newUserDto.email());
        verify(userMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toUserDto(any());
    }
    //====================================================================
    //DELETE USER TESTS
    @Test
    void deleteUser_shouldCallDelete_whenUserExists() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);

        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    //====================================================================
    //UPDATE USER TESTS
    @Test
    void updateUser_shouldUpdateAndReturnUserDto_whenUserExists()  {
        UUID userId = UUID.randomUUID();
        NewUserDto userToUpdate = new NewUserDto("Jane", "Doe",
                null, "jane@gmail.com");

        User existingUserEntity = new User();
        UserDto expectedUserDto = new UserDto(userId, "Jane", "Doe",
                null, "jane@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUserEntity));
        doNothing().when(userMapper).updateUserFromDto(userToUpdate, existingUserEntity);
        when(userRepository.save(existingUserEntity)).thenReturn(existingUserEntity);
        when(userMapper.toUserDto(existingUserEntity)).thenReturn(expectedUserDto);

        UserDto updatedUser = userService.updateUser(userId, userToUpdate);

        assertNotNull(updatedUser);
        assertEquals(expectedUserDto.email(), updatedUser.email());
        assertEquals(expectedUserDto.id(), userId);

        verify(userRepository).findById(userId);
        verify(userMapper).updateUserFromDto(userToUpdate, existingUserEntity);
        verify(userRepository).save(existingUserEntity);
        verify(userMapper).toUserDto(existingUserEntity);

    }

    @Test
    void updateUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        NewUserDto newUser = new NewUserDto("Jane", "Doe",
                null, "jane@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            userService.updateUser(userId, newUser));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).updateUserFromDto(any(), any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void updateUser_shouldThrowResourceCreationConflictException_whenUserWithEmailExists() {
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        NewUserDto userToUpdate = new NewUserDto("Jane", "Doe",
                null, "jane@gmail.com");

        User existingUserEntity = mock(User.class);
        User anotherUserWithSameEmail = mock(User.class);

        when(anotherUserWithSameEmail.getId()).thenReturn(anotherUserId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUserEntity));

        when(userRepository.findByEmail(userToUpdate.email())).thenReturn(Optional.of(anotherUserWithSameEmail));


        assertThatThrownBy(() -> userService.updateUser(userId, userToUpdate))
                .isInstanceOf(ResourceCreationConflictException.class)
                .hasMessage("Email " + userToUpdate.email() + " is already in use by another user.");


        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(userToUpdate.email());


        verify(userMapper, never()).updateUserFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }
    //====================================================================
    //GET USER TESTS
    @Test
    void getUserById_shouldReturnUserWithCards_whenUserExists() {

        UUID userId = UUID.randomUUID();
        User userEntity = new User();
        List<CardInfo> cardList = List.of(new CardInfo()); // Мок-список карт
        UserWithCardsDto expectedDto = new UserWithCardsDto(userId, "Test",
                null, null, "test@test.com", List.of());


        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(cardRepository.findByUserId(userId)).thenReturn(cardList);
        when(userMapper.toUserWithCardsDto(userEntity)).thenReturn(expectedDto);


        UserWithCardsDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(expectedDto.id(), result.id());
        verify(userRepository).findById(userId);
        verify(cardRepository).findByUserId(userId);
        verify(userMapper).toUserWithCardsDto(userEntity);
    }

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });


        verify(userRepository).findById(userId);
        verify(cardRepository, never()).findByUserId(any());
        verify(userMapper, never()).toUserWithCardsDto(any());
    }

    @Test
    void getUserByEmail_shouldReturnUserWithCards_whenUserExists() {
        String email = "test@example.com";
        User userEntity = mock(User.class);
        UUID userId = UUID.randomUUID();

        List<CardInfo> cardList = List.of(new CardInfo());
        UserWithCardsDto expectedDto = new UserWithCardsDto(userId, "Test", null, null, email, List.of());

        when(userEntity.getId()).thenReturn(userId);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(cardRepository.findByUserId(userId)).thenReturn(cardList);
        when(userMapper.toUserWithCardsDto(userEntity)).thenReturn(expectedDto);

        Optional<UserWithCardsDto> result = userService.getUserByEmail(email);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(dto -> {
                    assertThat(dto.email()).isEqualTo(email);
                    assertThat(dto.id()).isEqualTo(userId);
                });

        verify(userRepository).findByEmail(email);
        verify(cardRepository).findByUserId(userId);
        verify(userMapper).toUserWithCardsDto(userEntity);
    }

    @Test
    void getUserByEmail_shouldReturnEmptyOptional_whenUserDoesNotExist() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserWithCardsDto> result = userService.getUserByEmail(email);

        assertThat(result).isEmpty();

        verify(userRepository).findByEmail(email);
        verify(cardRepository, never()).findByUserId(any());
        verify(userMapper, never()).toUserWithCardsDto(any());
    }

    //====================================================================
    //GET MANY USERS TESTS
    @Test
    void getAllUsers_shouldReturnPageOfUserDtos() {
        Pageable pageable = Pageable.unpaged();
        User user = new User();
        user.setEmail("test@example.com");
        UserDto userDto = new UserDto(UUID.randomUUID(), "Test", null, null, "test@example.com");

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        Page<UserDto> result = userService.getAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("test@example.com");

        verify(userRepository).findAll(pageable);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void getUsersWithBirthdayToday_shouldReturnListOfUserDtos() {
        Instant fixedInstant = frozenDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        User user1 = new User();
        user1.setEmail("a@a.com");
        User user2 = new User();
        user2.setEmail("b@b.com");

        UserDto dto1 = new UserDto(UUID.randomUUID(), "A",
                null,
                LocalDate.of(1993, frozenDate.getMonth(), frozenDate.getDayOfMonth()),
                "a@a.com");
        UserDto dto2 = new UserDto(UUID.randomUUID(), "B",
                null,
                LocalDate.of(2005, frozenDate.getMonth(), frozenDate.getDayOfMonth()),
                "b@b.com");

        List<User> userList = List.of(user1, user2);

        when(userRepository.findUsersWithBirthDayToday(frozenDate)).thenReturn(userList);
        when(userMapper.toUserDto(user1)).thenReturn(dto1);
        when(userMapper.toUserDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.getUsersWithBirthdayToday();

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(UserDto::email)
                .containsExactly("a@a.com", "b@b.com");

        verify(userRepository).findUsersWithBirthDayToday(frozenDate);
        verify(userMapper).toUserDto(user1);
        verify(userMapper).toUserDto(user2);
    }

    @Test
    void getUsersBySearchTerm_shouldReturnPageOfUserDtos() {
        String searchTerm = "test";
        Pageable pageable = Pageable.unpaged();
        User user = new User();
        user.setEmail("test@example.com");
        UserDto userDto = new UserDto(UUID.randomUUID(), "Test", null, null, "test@example.com");

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(userPage);
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        Page<UserDto> result = userService.getUsersBySearchTerm(searchTerm, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().email()).isEqualTo("test@example.com");

        verify(userRepository).findBySearchTerm(searchTerm, pageable);
        verify(userMapper).toUserDto(user);
    }

    //====================================================================
    //CREATE CARD TESTS
    @Test
    void createCardForUser_shouldSaveCard_whenUserExists() {
        UUID userId = UUID.randomUUID();
        NewCardInfoDto newCardDto = new NewCardInfoDto("1234", "Holder", LocalDate.now());

        User userEntity = new User();
        CardInfo cardEntity = new CardInfo();
        CardInfo savedCardEntity = new CardInfo();
        CardInfoDto expectedCardDto = new CardInfoDto(UUID.randomUUID(), userId, "1234",
                "Holder", LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(cardInfoMapper.toCardInfo(newCardDto)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(savedCardEntity);
        when(cardInfoMapper.toCardInfoDto(savedCardEntity)).thenReturn(expectedCardDto);

        CardInfoDto result = userService.createCardForUser(userId, newCardDto);

        assertNotNull(result);
        assertEquals(expectedCardDto.cardNumber(), result.cardNumber());


        verify(userRepository).findById(userId);
        verify(cardInfoMapper).toCardInfo(newCardDto);

        assertNotNull(cardEntity.getUser());
        verify(cardRepository).save(cardEntity);
        verify(cardInfoMapper).toCardInfoDto(savedCardEntity);
    }

    @Test
    void createCardForUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();
        NewCardInfoDto newCardDto = new NewCardInfoDto("1234", "Holder", LocalDate.now());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> {
            userService.createCardForUser(userId, newCardDto);
        });

        verify(userRepository).findById(userId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createCardForUser_shouldThrowResourceCreationConflictException_whenCardWithSameNumberExists() {
        UUID userId = UUID.randomUUID();

        NewCardInfoDto newCardDto = new NewCardInfoDto("1234",
                "Holder", LocalDate.now());
        String number = newCardDto.cardNumber();

        User userEntity = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        CardInfo cardWithSameNumber = mock(CardInfo.class);
        when(cardRepository.findByNumber(number)).thenReturn(Optional.of(cardWithSameNumber));

        assertThatThrownBy(() -> userService.createCardForUser(userId, newCardDto))
                .isInstanceOf(ResourceCreationConflictException.class)
                .hasMessage("Card with number 1234 already exists.");

        verify(userRepository).findById(userId);
        verify(cardRepository).findByNumber(number);
        verify(cardRepository, never()).save(any());
    }



        //====================================================================
    //DELETE CARD TESTS
    @Test
    void deleteCard_shouldCallDelete_whenCardExists() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.existsById(cardId)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(cardId);

        userService.deleteCard(cardId);

        verify(cardRepository).existsById(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_shouldThrowResourceNotFoundException_whenCardDoesNotExist() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteCard(cardId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardRepository).existsById(cardId);
        verify(cardRepository, never()).deleteById(any());
    }

    //GET CARD TESTS
    @Test
    void getCardById_shouldReturnCardInfoDto_whenCardExists() {
        UUID cardId = UUID.randomUUID();
        CardInfo cardEntity = new CardInfo();
        CardInfoDto expectedDto = new CardInfoDto(cardId, UUID.randomUUID(), "1234", "Holder", LocalDate.now());

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardEntity));
        when(cardInfoMapper.toCardInfoDto(cardEntity)).thenReturn(expectedDto);

        CardInfoDto result = userService.getCardById(cardId);

        assertThat(result)
                .isNotNull()
                .satisfies(dto -> {
                    assertThat(dto.id()).isEqualTo(cardId);
                    assertThat(dto.cardNumber()).isEqualTo("1234");
                });

        verify(cardRepository).findById(cardId);
        verify(cardInfoMapper).toCardInfoDto(cardEntity);
    }

    @Test
    void getCardById_shouldThrowNotFoundException_whenCardDoesNotExist() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCardById(cardId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardRepository).findById(cardId);
        verify(cardInfoMapper, never()).toCardInfoDto(any());
    }

    @Test
    void getCardByNumber_shouldReturnCardInfoDto_whenCardExists() {
        String cardNumber = "1234-5678-9012-3456";
        CardInfo cardEntity = new CardInfo();
        CardInfoDto expectedDto = new CardInfoDto(UUID.randomUUID(), UUID.randomUUID(), cardNumber, "Holder", LocalDate.now());

        when(cardRepository.findByNumber(cardNumber)).thenReturn(Optional.of(cardEntity));
        when(cardInfoMapper.toCardInfoDto(cardEntity)).thenReturn(expectedDto);

        Optional<CardInfoDto> result = userService.getCardByNumber(cardNumber);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(dto -> {
                    assertThat(dto.cardNumber()).isEqualTo(cardNumber);
                });

        verify(cardRepository).findByNumber(cardNumber);
        verify(cardInfoMapper).toCardInfoDto(cardEntity);
    }

    @Test
    void getCardByNumber_shouldReturnEmptyOptional_whenCardDoesNotExist() {
        String cardNumber = "0000-0000-0000-0000";

        when(cardRepository.findByNumber(cardNumber)).thenReturn(Optional.empty());

        Optional<CardInfoDto> result = userService.getCardByNumber(cardNumber);

        assertThat(result).isEmpty();

        verify(cardRepository).findByNumber(cardNumber);
        verify(cardInfoMapper, never()).toCardInfoDto(any());
    }


    //====================================================================
    //GET MANY CARDS TESTS
    @Test
    void getCardsByUserId_shouldReturnListOfCardInfoDtos() {
        UUID userId = UUID.randomUUID();
        List<CardInfo> cardList = List.of(new CardInfo(), new CardInfo());
        List<CardInfoDto> expectedDtoList = List.of(
                new CardInfoDto(UUID.randomUUID(), userId, "1111", "Holder1", LocalDate.now()),
                new CardInfoDto(UUID.randomUUID(), userId, "2222", "Holder2", LocalDate.now())
        );

        when(cardRepository.findByUserId(userId)).thenReturn(cardList);
        when(cardInfoMapper.toCardInfoDtoList(cardList)).thenReturn(expectedDtoList);

        List<CardInfoDto> result = userService.getCardsByUserId(userId);

        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(CardInfoDto::cardNumber)
                .containsExactly("1111", "2222");

        verify(cardRepository).findByUserId(userId);
        verify(cardInfoMapper).toCardInfoDtoList(cardList);
    }

    @Test
    void getExpiredCards_shouldReturnListOfExpiredCardInfoDtos() {
        Instant fixedInstant = frozenDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        List<CardInfo> expiredCardList = List.of(new CardInfo());
        List<CardInfoDto> expectedDtoList = List.of(
                new CardInfoDto(UUID.randomUUID(),
                        UUID.randomUUID(),
                        "9999",
                        "Expired Holder",
                        frozenDate.minusDays(1))
        );

        when(cardRepository.findExpiredCardsNative(frozenDate)).thenReturn(expiredCardList);
        when(cardInfoMapper.toCardInfoDtoList(expiredCardList)).thenReturn(expectedDtoList);

        List<CardInfoDto> result = userService.getExpiredCards();

        assertThat(result)
                .isNotNull()
                .hasSize(1);
        assertThat(result.get(0).cardNumber()).isEqualTo("9999");

        verify(cardRepository).findExpiredCardsNative(frozenDate);
        verify(cardInfoMapper).toCardInfoDtoList(expiredCardList);
    }
}