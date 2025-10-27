package io.hohichh.marketplace.user.service;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto createUser(NewUserDto user);
    void deleteUser(UUID id) throws NotFoundException;
    UserDto updateUser(UUID id, NewUserDto user) throws NotFoundException;
    UserWithCardsDto getUserById(UUID id) throws NotFoundException;

    Optional<UserWithCardsDto> getUserByEmail(String email);

    Page<UserDto> getAllUsers(Pageable pageable);
    List<UserDto> getUsersWithBirthdayToday();
    Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable);

    CardInfoDto createCardForUser(CardInfoDto cardInfo) throws NotFoundException;
    void deleteCard(UUID cardId) throws NotFoundException;
    CardInfoDto getCardById(UUID cardId) throws NotFoundException;

    Optional<CardInfoDto> getCardByNumber(String cardNumber);

    List<CardInfoDto> getCardsByUserId(UUID userId);
    List<CardInfoDto> getExpiredCards();
}
