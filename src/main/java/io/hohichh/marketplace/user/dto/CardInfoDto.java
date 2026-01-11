package io.hohichh.marketplace.user.dto;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record CardInfoDto (
    @NotNull UUID id,
    @NotNull UUID userId,

    @NotBlank
    @Size(max = 64)
    String cardNumber,

    @NotBlank
    @Size(max = 255)
    String cardHolderName,

    @NotNull
    @Future
    LocalDate expirationDate) implements Serializable{
}
