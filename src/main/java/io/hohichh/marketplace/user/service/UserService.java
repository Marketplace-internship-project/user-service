package io.hohichh.marketplace.user.service;

import io.hohichh.marketplace.user.dto.*;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto createUser(NewUserDto user);
    void deleteUser(UUID id) ;
    UserDto updateUser(UUID id, NewUserDto user);
    UserWithCardsDto getUserById(UUID id) ;

    Optional<UserWithCardsDto> getUserByEmail(String email);

    Page<UserDto> getAllUsers(Pageable pageable);
    List<UserDto> getUsersWithBirthdayToday();
    Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable);

    CardInfoDto createCardForUser(UUID userId, NewCardInfoDto cardInfo);
    void deleteCard(UUID cardId) ;
    CardInfoDto getCardById(UUID cardId);

    Optional<CardInfoDto> getCardByNumber(String cardNumber);

    List<CardInfoDto> getCardsByUserId(UUID userId);
    List<CardInfoDto> getExpiredCards();
}
