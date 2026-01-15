package io.hohichh.marketplace.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.hohichh.marketplace.user.dto.registration.NewUserCredsDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
        "application.config.auth-url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
class UserRegistrationTest extends AbstractApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

//    @DynamicPropertySource
//    static void configureProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
//        registry.add("application.config.auth-url",
//                () -> "http://localhost:" +
//                        org.springframework.cloud.contract.wiremock.
//                                WireMockSpring.options().port(0));
//    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldReturnCreated_andCallAuthService() throws Exception {
        NewUserCredsDto requestDto = new NewUserCredsDto(
                "Adam", "Smith", LocalDate.of(1990, 1, 1),
                "adam@test.com", "login", "password"
        );


        stubFor(WireMock.post(urlEqualTo("/api/v1/auth/credentials"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"userId\": \"" + UUID.randomUUID() + "\"}")));


        mockMvc.perform(post("/v1/registration/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.email").value("adam@test.com"))
                .andExpect(jsonPath("$.id").exists());

        verify(postRequestedFor(urlEqualTo("/api/v1/auth/credentials")));
    }

    @Test
    void registerUser_shouldReturnConflict_whenAuthServiceFails() throws Exception {
        NewUserCredsDto requestDto = new NewUserCredsDto(
                "Eve", "Smith", LocalDate.of(1990, 1, 1),
                "eve@test.com", "busy_login", "password"
        );

        stubFor(WireMock.post(urlEqualTo("/api/v1/auth/credentials"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withBody("Login already exists")));

        mockMvc.perform(post("/v1/registration/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Login already exists")); // Или как у вас настроено в ExceptionHandler
    }
}