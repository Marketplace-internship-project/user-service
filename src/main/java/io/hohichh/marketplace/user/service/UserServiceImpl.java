/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement service layer for user and card management
 */

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link UserService} interface.
 * Handles the business logic for managing users and their payment cards,
 * interacting with the repositories.
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    private final UserMapper userMapper;
    private final CardInfoMapper cardInfoMapper;

    /**
     * Constructs a new UserServiceImpl with the required repositories and mappers.
     *
     * @param userRepository Repository for user data access.
     * @param cardRepository Repository for card data access.
     * @param userMapper     Mapper for user entity/DTO conversion.
     * @param cardInfoMapper Mapper for card entity/DTO conversion.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CardRepository cardRepository,
                           UserMapper userMapper,
                           CardInfoMapper cardInfoMapper) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.userMapper = userMapper;
        this.cardInfoMapper = cardInfoMapper;
    }

    /**
     * {@inheritDoc}
     * Checks if a user with the same email already exists before creation.
     *
     * @throws ResourceCreationConflictException if the email is already in use.
     */
    @Override
    @Transactional
    public UserDto createUser(NewUserDto user) {
        String email = user.email();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceCreationConflictException("User with email " + email + " already exists.");
        }
        User savedUser = userRepository.save(
                userMapper.toUser(user));

        return userMapper.toUserDto(savedUser);
    }

    /**
     * {@inheritDoc}
     * Checks if the user exists before attempting deletion.
     *
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }
        userRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     * Finds the existing user, validates the new email for uniqueness (if changed), and applies updates.
     *
     * @throws ResourceNotFoundException       if the user with the specified ID is not found.
     * @throws ResourceCreationConflictException if the new email is already in use by another user.
     */
    @Override
    @Transactional
    public UserDto updateUser(UUID id, NewUserDto userToUpd) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        String newEmail = userToUpd.email();
        Optional<User> userWithSameEmail = userRepository.findByEmail(newEmail);
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            throw new ResourceCreationConflictException("Email " + newEmail + " is already in use by another user.");
        }

        userMapper.updateUserFromDto(userToUpd, existingUser);

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toUserDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     * Retrieves the user and their associated cards from the repositories.
     *
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public UserWithCardsDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        List<CardInfo> cards = cardRepository.findByUserId(id);

        return userMapper.toUserWithCardsDto(user, cards);
    }

    /**
     * {@inheritDoc}
     * Finds the user by email and, if found, fetches their associated cards.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserWithCardsDto> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        List<CardInfo> cards = cardRepository.findByUserId(user.getId());

        return Optional.of(
                userMapper.toUserWithCardsDto(user, cards));
    }

    /**
     * {@inheritDoc}
     * Maps the resulting Page of User entities to a Page of UserDto.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        return userPage.map(userMapper::toUserDto);
    }

    /**
     * {@inheritDoc}
     * Delegates to the repository to find users with a matching birthday and maps the results.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersWithBirthdayToday() {
        List<User> users = userRepository.findUsersWithBirthDayToday();

        return users.stream().map(userMapper::toUserDto).toList();
    }

    /**
     * {@inheritDoc}
     * Delegates to the repository to perform the search and maps the resulting page.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable) {
        Page<User> userPage = userRepository.findBySearchTerm(searchTerm, pageable);

        return userPage.map(userMapper::toUserDto);
    }

    /**
     * {@inheritDoc}
     * Finds the user, checks if the card number already exists for another user,
     * and then associates the new card with the user.
     *
     * @throws ResourceNotFoundException       if the user with the specified ID is not found.
     * @throws ResourceCreationConflictException if the card number is already associated with another user.
     */
    @Override
    @Transactional
    public CardInfoDto createCardForUser(UUID userId, NewCardInfoDto newCard) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found."));

        String number = newCard.cardNumber();
        Optional<CardInfo> existingCard = cardRepository.findByNumber(number);
        if (existingCard.isPresent() && !existingCard.get().getUser().getId().equals(userId)) {
            throw new ResourceCreationConflictException("Card with number " + number + " already exists.");
        }

        CardInfo cardInfoEntity = cardInfoMapper.toCardInfo(newCard);
        cardInfoEntity.setUser(user);

        CardInfo savedCard = cardRepository.save(cardInfoEntity);

        return cardInfoMapper.toCardInfoDto(savedCard);
    }

    /**
     * {@inheritDoc}
     * Checks if the card exists before attempting deletion.
     *
     * @throws ResourceNotFoundException if the card with the specified ID is not found.
     */
    @Override
    @Transactional
    public void deleteCard(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new ResourceNotFoundException("Card with id " + cardId + " not found.");
        }
        cardRepository.deleteById(cardId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the card with the specified ID is not found.
     */
    @Override
    @Transactional(readOnly = true)
    public CardInfoDto getCardById(UUID cardId) {
        CardInfo cardInfo = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card with id " + cardId + " not found."));

        return cardInfoMapper.toCardInfoDto(cardInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CardInfoDto> getCardByNumber(String cardNumber) {
        Optional<CardInfo> cardInfoOpt = cardRepository.findByNumber(cardNumber);
        if (cardInfoOpt.isEmpty()) {
            return Optional.empty();
        }

        CardInfoDto cardInfoDto = cardInfoMapper.toCardInfoDto(cardInfoOpt.get());
        return Optional.of(cardInfoDto);
    }

    /**
     * {@inheritDoc}
     * This implementation retrieves cards based on the user ID. If the user ID does not exist,
     * it will return an empty list rather than throwing an exception.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getCardsByUserId(UUID userId) {
        List<CardInfo> cards = cardRepository.findByUserId(userId);
        return cardInfoMapper.toCardInfoDtoList(cards);
    }

    /**
     * {@inheritDoc}
     * Uses a repository method (likely a custom query) to find all cards
     * where the expiration date is in the past.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getExpiredCards() {
        List<CardInfo> expiredCards = cardRepository.findExpiredCardsNative();

        return cardInfoMapper.toCardInfoDtoList(expiredCards);
    }
}