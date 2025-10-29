package io.hohichh.marketplace.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record NewCardInfoDto(
        @NotBlank
        @Size(max = 64)
    String cardNumber,
        @NotBlank
        @Size(max = 255)
    String cardHolderName,
        @NotNull
    LocalDate expirationDate) implements Serializable {
}
