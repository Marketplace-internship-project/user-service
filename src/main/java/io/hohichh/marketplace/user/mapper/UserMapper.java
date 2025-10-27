package io.hohichh.marketplace.user.mapper;


import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.model.CardInfo;
import io.hohichh.marketplace.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(NewUserDto newUserDto);

    UserDto toUserDto(User user);

    @Mapping(target = "id", ignore = true)
    void updateUserFromDto(NewUserDto userDto, @MappingTarget User user);

    @Mapping(target = "cards", source = "cards")
    UserWithCardsDto toUserWithCardsDto(User user, List<CardInfo> cards);
}
