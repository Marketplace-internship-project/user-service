package io.hohichh.marketplace.user.dto.registration;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthServiceResponse(
        @NotNull
        UUID userId
) implements Serializable {
}
