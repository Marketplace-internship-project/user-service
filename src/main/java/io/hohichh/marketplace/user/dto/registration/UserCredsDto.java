package io.hohichh.marketplace.user.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

public record UserCredsDto(
        @NotNull
        UUID userId,

        @NotNull
        @NotBlank
        String login,

        @NotNull
        @NotBlank
        String password
) implements Serializable {
}
