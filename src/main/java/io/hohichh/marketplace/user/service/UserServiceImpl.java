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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
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

    private final Clock clock;
    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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
                           CardInfoMapper cardInfoMapper,
                           Clock clock) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.userMapper = userMapper;
        this.cardInfoMapper = cardInfoMapper;
        this.clock = clock;

        logger.trace("UserServiceImpl initialized with UserRepository and CardRepository");
    }

    /**
     * {@inheritDoc}
     * Checks if a user with the same email already exists before creation.
     *
     * @throws ResourceCreationConflictException if the email is already in use.
     */
    @Override
    @Transactional
    @CacheEvict(value = "usersWithBirthdayToday", allEntries = true)
    public UserDto createUser(NewUserDto user) {
        logger.debug("Attempting to create user with email: {}", user.email());

        String email = user.email();
        if (userRepository.findByEmail(email).isPresent()) {
            logger.error("User creation failed: email {} already exists", email);
            throw new ResourceCreationConflictException("User with email " + email + " already exists.");
        }

        User savedUser = userRepository.save(
                userMapper.toUser(user));

        logger.info("User with id: {} saved successfully", savedUser.getId());
        return userMapper.toUserDto(savedUser);
    }

    /**
     * {@inheritDoc}
     * Checks if the user exists before attempting deletion.
     *
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    @Override
    @PreAuthorize("hasRole('USER') and #id.toString() == authentication.name")
    @Transactional
    @CacheEvict(value = {"users", "usersWithBirthdayToday"}, key = "#id", allEntries = true)
    public void deleteUser(UUID id) {
        logger.debug("Attempting to delete user with id: {}", id);

        if (!userRepository.existsById(id)) {
            logger.error("User deletion failed: user with id {} not found", id);
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }

        logger.info("User with id: {} deleted successfully", id);
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
    @PreAuthorize("hasRole('USER') and #id.toString() == authentication.name")
    @Transactional
    @CacheEvict(value = {"users", "usersWithBirthdayToday"}, key = "#id", allEntries = true)
    public UserDto updateUser(UUID id, NewUserDto userToUpd) {
        logger.debug("Attempting to update user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User update failed: user with id {} not found", id);
                    return new ResourceNotFoundException("User with id " + id + " not found.");
                });

        String newEmail = userToUpd.email();
        Optional<User> userWithSameEmail = userRepository.findByEmail(newEmail);
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            logger.error("User update failed: email {} already in use by another user", newEmail);
            throw new ResourceCreationConflictException("Email " + newEmail + " is already in use by another user.");
        }

        userMapper.updateUserFromDto(userToUpd, existingUser);

        User updatedUser = userRepository.save(existingUser);

        logger.info("User with id: {} updated successfully", id);
        return userMapper.toUserDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     * Retrieves the user and their associated cards from the repositories.
     *
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    @Override
    @PreAuthorize("hasRole('USER') and #id.toString() == authentication.name")
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserWithCardsDto getUserById(UUID id) {
        logger.debug("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User fetch failed: user with id {} not found", id);
                    return new ResourceNotFoundException("User with id " + id + " not found.");
                });

        List<CardInfo> cards = cardRepository.findByUserId(id);

        logger.info("User with id: {} fetched successfully", id);
        return userMapper.toUserWithCardsDto(user);
    }

    /**
     * {@inheritDoc}
     * Finds the user by email and, if found, fetches their associated cards.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Optional<UserWithCardsDto> getUserByEmail(String email) {
        logger.debug("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            logger.debug("User with email: {} not found", email);
            return Optional.empty();
        }
        List<CardInfo> cards = cardRepository.findByUserId(user.getId());

        logger.info("User with id: {} fetched successfully by email", user.getId());
        return Optional.of(
                userMapper.toUserWithCardsDto(user));
    }

    /**
     * {@inheritDoc}
     * Maps the resulting Page of User entities to a Page of UserDto.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        if (pageable.isPaged()) {
            logger.debug("Fetching all users with pagination: page number {}, page size {}",
                    pageable.getPageNumber(), pageable.getPageSize());
        } else {
            logger.debug("Fetching all users (unpaged)");
        }

        Page<User> userPage = userRepository.findAll(pageable);

        logger.info("Fetched {} users", userPage.getNumberOfElements());
        return userPage.map(userMapper::toUserDto);
    }

    /**
     * {@inheritDoc}
     * Delegates to the repository to find users with a matching birthday and maps the results.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "usersWithBirthdayToday")
    public List<UserDto> getUsersWithBirthdayToday() {
        logger.debug("Fetching users with birthday today");
        LocalDate today = LocalDate.now(clock);
        List<User> users = userRepository.findUsersWithBirthDayToday(today);

        logger.info("Fetched {} users with birthday today", users.size());
        return users.stream().map(userMapper::toUserDto).toList();
    }

    /**
     * {@inheritDoc}
     * Delegates to the repository to perform the search and maps the resulting page.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersBySearchTerm(String searchTerm, Pageable pageable) {
        if (pageable.isPaged()) {
            logger.debug("Searching users with term: '{}' with pagination: page number {}, page size {}",
                    searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        } else {
            logger.debug("Fetching all users with search term: '{}' (unpaged)", searchTerm);
        }

        Page<User> userPage = userRepository.findBySearchTerm(searchTerm, pageable);

        logger.info("Found {} users by search term successfully", userPage.getNumberOfElements());
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
    @PreAuthorize("hasRole('USER') and #userId.toString() == authentication.name")
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public CardInfoDto createCardForUser(UUID userId, NewCardInfoDto newCard) {
        logger.debug("Attempting to create card for user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Card creation failed: user with id {} not found", userId);
                    return new ResourceNotFoundException("User with id " + userId + " not found.");
                });

        String number = newCard.cardNumber();
        if (cardRepository.findByNumber(number).isPresent()) {
            logger.error("Card creation failed: card with number {} already exists", number);
            throw new ResourceCreationConflictException("Card with number " + number + " already exists.");
        }

        CardInfo cardInfoEntity = cardInfoMapper.toCardInfo(newCard);
        cardInfoEntity.setUser(user);
        user.getCards().add(cardInfoEntity);

        CardInfo savedCard = cardRepository.save(cardInfoEntity);

        logger.info("Card with id: {} created successfully for user with id: {}", savedCard.getId(), userId);
        return cardInfoMapper.toCardInfoDto(savedCard);
    }

    /**
     * {@inheritDoc}
     * Checks if the card exists before attempting deletion.
     *
     * @throws ResourceNotFoundException if the card with the specified ID is not found.
     */
    @Override
    @PreAuthorize("hasRole('USER') and @userAndCardSecurity.isCardOwner(#cardId, authentication)")
    @Transactional
    public void deleteCard(UUID cardId) {
        logger.debug("Attempting to delete card with id: {}", cardId);

        if (!cardRepository.existsById(cardId)) {
            logger.error("Card deletion failed: card with id {} not found", cardId);
            throw new ResourceNotFoundException("Card with id " + cardId + " not found.");
        }

        logger.debug("Card with id: {} deleted successfully", cardId);
        cardRepository.deleteById(cardId);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException if the card with the specified ID is not found.
     */
    @Override
    @PostAuthorize("hasRole('USER') and returnObject.userId().toString() == authentication.name")
    @Transactional(readOnly = true)
    public CardInfoDto getCardById(UUID cardId) {
        logger.debug("Fetching card with id: {}", cardId);

        CardInfo cardInfo = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    logger.error("Card fetch failed: card with id {} not found", cardId);
                    return new ResourceNotFoundException("Card with id " + cardId + " not found.");
                });

        logger.info("Card with id: {} fetched successfully", cardId);
        return cardInfoMapper.toCardInfoDto(cardInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Optional<CardInfoDto> getCardByNumber(String cardNumber) {
        logger.debug("Fetching card with number: {}", cardNumber);

        Optional<CardInfo> cardInfoOpt = cardRepository.findByNumber(cardNumber);
        if (cardInfoOpt.isEmpty()) {
            logger.debug("Card with number: {} not found", cardNumber);
            return Optional.empty();
        }

        CardInfoDto cardInfoDto = cardInfoMapper.toCardInfoDto(cardInfoOpt.get());

        logger.info("Card with id: {} fetched successfully by number", cardInfoDto.id());
        return Optional.of(cardInfoDto);
    }

    /**
     * {@inheritDoc}
     * This implementation retrieves cards based on the user ID. If the user ID does not exist,
     * it will return an empty list rather than throwing an exception.
     */
    @Override
    @PreAuthorize("hasRole('USER') and #userId.toString() == authentication.name")
    @Transactional(readOnly = true)
    public List<CardInfoDto> getCardsByUserId(UUID userId) {
        logger.debug("Fetching cards for user with id: {}", userId);

        List<CardInfo> cards = cardRepository.findByUserId(userId);

        logger.info("Fetched {} cards for user with id: {}", cards.size(), userId);
        return cardInfoMapper.toCardInfoDtoList(cards);
    }

    /**
     * {@inheritDoc}
     * Uses a repository method (likely a custom query) to find all cards
     * where the expiration date is in the past.
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "expiredCards")
    public List<CardInfoDto> getExpiredCards() {
        logger.debug("Fetching expired cards");
        LocalDate today = LocalDate.now(clock);
        List<CardInfo> expiredCards = cardRepository.findExpiredCardsNative(today);

        logger.info("Fetched {} expired cards", expiredCards.size());
        return cardInfoMapper.toCardInfoDtoList(expiredCards);
    }
}