package io.hohichh.marketplace.user.service;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.repository.CardRepository;
import io.hohichh.marketplace.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public UserDto createUser(NewUserDto user) {
        return null;
    }

    @Override
    public void deleteUser(UUID id) throws NotFoundException {

    }

    @Override
    public UserDto updateUser(UUID id, NewUserDto user) throws NotFoundException {
        return null;
    }

    @Override
    public UserWithCardsDto getUserById(UUID id) throws NotFoundException {
        return null;
    }

    @Override
    public Optional<UserWithCardsDto> getUserByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return null;
    }

    @Override
    public List<UserDto> getUsersWithBirthdayToday() {
        return List.of();
    }

    @Override
    public Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public CardInfoDto createCardForUser(CardInfoDto cardInfo) throws NotFoundException {
        return null;
    }

    @Override
    public void deleteCard(UUID cardId) throws NotFoundException {

    }

    @Override
    public CardInfoDto getCardById(UUID cardId) throws NotFoundException {
        return null;
    }

    @Override
    public Optional<CardInfoDto> getCardByNumber(String cardNumber) {
        return Optional.empty();
    }

    @Override
    public List<CardInfoDto> getCardsByUserId(UUID userId) {
        return List.of();
    }

    @Override
    public List<CardInfoDto> getExpiredCards() {
        return List.of();
    }
}
