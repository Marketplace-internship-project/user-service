package io.hohichh.marketplace.user.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record NewUserDto(
        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String surname,

        @Past(message = "birthDate must be in the past")
        LocalDate birthDate,

        @NotBlank
        @Email
        @Size(max = 255)
        String email) {
}
