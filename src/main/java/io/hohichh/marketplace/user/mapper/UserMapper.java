/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement mapping between User entity and related DTOs
 */

package io.hohichh.marketplace.user.mapper;


import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.model.CardInfo;
import io.hohichh.marketplace.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


/**
 * A MapStruct mapper interface for converting between the {@link User} entity
 * and its related DTOs ({@link UserDto}, {@link NewUserDto}, {@link UserWithCardsDto}).
 * <p>
 * This mapper is managed by Spring ({@code componentModel = "spring"}).
 */
@Mapper(componentModel = "spring", uses = {CardInfoMapper.class})
public interface UserMapper {

    /**
     * Converts a {@link NewUserDto} (used for creation/update) to a {@link User} entity.
     *
     * @param newUserDto The DTO containing new user data.
     * @return The corresponding User entity.
     */
    @Mapping(target = "cards", ignore = true)
    User toUser(NewUserDto newUserDto);

    /**
     * Converts a {@link User} entity to a standard {@link UserDto}.
     *
     * @param user The entity to convert.
     * @return The resulting UserDto.
     */
    UserDto toUserDto(User user);

    /**
     * Updates an existing {@link User} entity from a {@link NewUserDto}.
     * The {@code @MappingTarget} annotation ensures the existing object is modified.
     * The 'id' field is ignored to prevent changing the entity's primary key.
     *
     * @param userDto The DTO containing the updated data.
     * @param user    The existing User entity (target) to update.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateUserFromDto(NewUserDto userDto, @MappingTarget User user);

    /**
     * Combines a {@link User} entity and a list of their {@link CardInfo} entities
     * into a single {@link UserWithCardsDto}.
     *
     * @param user  The user entity.
     * @return The resulting UserWithCardsDto.
     */
    @Mapping(target = "cards", source = "cards")
    UserWithCardsDto toUserWithCardsDto(User user);
}