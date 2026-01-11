package io.hohichh.marketplace.user.dto;

import jakarta.validation.constraints.*;

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
        @Future
    LocalDate expirationDate) implements Serializable {
}
