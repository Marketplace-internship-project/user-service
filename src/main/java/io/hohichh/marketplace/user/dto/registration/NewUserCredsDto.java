package io.hohichh.marketplace.user.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

public record NewUserCredsDto(
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
        String email,

        @NotBlank
        String login,

        @NotBlank
        String password
) implements Serializable {
}
