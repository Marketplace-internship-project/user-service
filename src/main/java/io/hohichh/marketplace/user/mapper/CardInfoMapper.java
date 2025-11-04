/*
 * Author: Yelizaveta Verkovich aka Hohich
 * Task: Implement mapping between CardInfo entity and DTOs
 */

package io.hohichh.marketplace.user.mapper;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.model.CardInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * A MapStruct mapper interface for converting between the {@link CardInfo} entity
 * and its related DTOs ({@link CardInfoDto}, {@link NewCardInfoDto}).
 * <p>
 * This mapper is managed by Spring ({@code componentModel = "spring"}).
 */
@Mapper(componentModel = "spring")
public interface CardInfoMapper {

    /**
     * Converts a {@link CardInfo} entity to a {@link CardInfoDto}.
     * Manually maps fields with different names (e.g., 'number' to 'cardNumber').
     *
     * @param cardInfo The entity to convert.
     * @return The resulting CardInfoDto.
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cardNumber", source = "number")
    @Mapping(target = "cardHolderName", source = "holder")
    @Mapping(target = "expirationDate", source = "expiryDate")
    CardInfoDto toCardInfoDto(CardInfo cardInfo);


    /**
     * Converts a list of {@link CardInfo} entities to a list of {@link CardInfoDto}s.
     *
     * @param cardInfoList The list of entities.
     * @return The list of DTOs.
     */
    List<CardInfoDto> toCardInfoDtoList(List<CardInfo> cardInfoList);


    /**
     * Converts a {@link NewCardInfoDto} (used for creating new cards) to a {@link CardInfo} entity.
     * The 'user' field is ignored, as it must be set manually in the service layer.
     *
     * @param newCardDto The creation DTO.
     * @return The resulting CardInfo entity.
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", source = "cardNumber")
    @Mapping(target = "holder", source = "cardHolderName")
    @Mapping(target = "expiryDate", source = "expirationDate")
    CardInfo toCardInfo(NewCardInfoDto newCardDto);

}