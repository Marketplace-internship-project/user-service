package io.hohichh.marketplace.user.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UserWithCardsDto(
                               @NotNull UUID id,
                               @NotBlank @Size(max = 255) String name,
                               @Size(max = 255) String surname,
                               @Past LocalDate birthDate,
                               @Email @NotBlank @Size(max = 255) String email,
                               @NotNull @Valid List<CardInfoDto> cards) {
}
