package io.hohichh.marketplace.user.mapper;

import io.hohichh.marketplace.user.dto.CardInfoDto;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.model.CardInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardInfoMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cardNumber", source = "number")
    @Mapping(target = "cardHolderName", source = "holder")
    @Mapping(target = "expirationDate", source = "expiryDate")
    CardInfoDto toCardInfoDto(CardInfo cardInfo);


    List<CardInfoDto> toCardInfoDtoList(List<CardInfo> cardInfoList);


    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", source = "cardNumber")
    @Mapping(target = "holder", source = "cardHolderName")
    @Mapping(target = "expiryDate", source = "expirationDate")
    CardInfo toCardInfo(NewCardInfoDto newCardDto);

}