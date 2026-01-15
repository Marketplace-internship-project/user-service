package io.hohichh.marketplace.user.webclient;

import io.hohichh.marketplace.user.dto.registration.AuthServiceResponse;
import io.hohichh.marketplace.user.dto.registration.UserCredsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="auth-service",
        url = "${application.config.auth-url}",
        path = "/api"
)
public interface AuthServiceClient {

    @PostMapping("/v1/auth/credentials")
    AuthServiceResponse createCredentials(@RequestBody UserCredsDto credentials);
}
