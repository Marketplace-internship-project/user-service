
package io.hohichh.marketplace.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hohichh.marketplace.user.dto.NewCardInfoDto;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.integration.config.TestClockConfiguration;
import io.hohichh.marketplace.user.integration.config.TestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
        TestContainerConfiguration.class,
        TestClockConfiguration.class
})
class UserSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    @BeforeEach
    void setUpClock() {
        Instant fixedInstant = Instant.parse("2025-10-30T10:00:00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    //-------------------PUBLIC ENDPOINTS
    @Test
    void createUser_shouldSucceed_whenEndpointIsPublic() throws Exception {
        NewUserDto newUser = new NewUserDto(
                "Public",
                "User",
                LocalDate.of(2000, 1, 1),
                "public.user@example.com"
        );

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated());
    }

    //-------------------------ADMIN ENDPOINTS
    //----------------getAll
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getAllUsers_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getAllUsers_shouldFail_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_shouldFail_whenPublic() throws Exception {
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isForbidden());
    }

    //----------------getAllWithSearchTerm
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getAllUsersWithSearchTerm_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/users?searchTerm=admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getAllUsersWithSearchTerm_shouldFail_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/users?searchTerm=user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsersWithSearchTerm_shouldFail_whenPublic() throws Exception {
        mockMvc.perform(get("/v1/users?searchTerm=user"))
                .andExpect(status().isForbidden());
    }

    //-------------getAllWithBirthdayToday
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getAllUsersWithBirthdayToday_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/users?birth-date=today"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getAllUsersWithBirthdayToday_shouldFailed_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/users?birth-date=today"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsersWithBirthdayToday_shouldFailed_whenPublic() throws Exception {
        mockMvc.perform(get("/v1/users?birth-date=today"))
                .andExpect(status().isForbidden());
    }

    //----------------getUserByEmail
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getUserByEmail_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/users?email=admin@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getUserByEmail_shouldFail_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/users?email=user@example.com"))
                .andExpect(status().isForbidden());
    }

    //----------------getCardByNumber
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getCardByNumber_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/cards?number=12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getCardByNumber_shouldFail_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/cards?number=12345"))
                .andExpect(status().isForbidden());
    }

    //----------------getExpiredCards
    @Test
    @WithMockUser(username = "test-admin", roles = "ADMIN")
    void getExpiredCards_shouldSucceed_whenRoleIsAdmin() throws Exception {
        mockMvc.perform(get("/v1/cards?expiration-date=today"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user", roles = "USER")
    void getExpiredCards_shouldFail_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/v1/cards?expiration-date=today"))
                .andExpect(status().isForbidden());
    }


    //------------------------------USER-ENDPOINTS
    private final String selfUserId = "00000000-0000-0000-0000-000000000001";
    private final String otherUserId = "00000000-0000-0000-0000-000000000002";

    //----------------getUserById
    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void getUserById_shouldSucceed_whenGettingSelf() throws Exception {
        // @PreAuthorize("...#id.toString() == authentication.name")
        mockMvc.perform(get("/v1/users/" + selfUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void getUserById_shouldFail_whenGettingOther() throws Exception {
        mockMvc.perform(get("/v1/users/" + otherUserId))
                .andExpect(status().isForbidden());
    }

    //----------------updateUser
    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void updateUser_shouldSucceed_whenUpdatingSelf() throws Exception {
        // @PreAuthorize("...#id.toString() == authentication.name")
        NewUserDto userDto = new NewUserDto("name", "surname", null, "email@email.com");

        mockMvc.perform(put("/v1/users/" + selfUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void updateUser_shouldFail_whenUpdatingOther() throws Exception {
        NewUserDto userDto = new NewUserDto("name", "surname", null, "email@email.com");

        mockMvc.perform(put("/v1/users/" + otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    //----------------deleteUser
    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void deleteUser_shouldSucceed_whenDeletingSelf() throws Exception {
        // @PreAuthorize("...#id.toString() == authentication.name")
        mockMvc.perform(delete("/v1/users/" + selfUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void deleteUser_shouldFail_whenDeletingOther() throws Exception {
        mockMvc.perform(delete("/v1/users/" + otherUserId))
                .andExpect(status().isForbidden());
    }

    //----------------getCardsByUserId
    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void getCardsByUserId_shouldSucceed_whenGettingSelf() throws Exception {
        // @PreAuthorize("...#userId.toString() == authentication.name")
        mockMvc.perform(get("/v1/users/" + selfUserId + "/cards"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void getCardsByUserId_shouldFail_whenGettingOther() throws Exception {
        mockMvc.perform(get("/v1/users/" + otherUserId + "/cards"))
                .andExpect(status().isForbidden());
    }

    //----------------createCardForUser
    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void createCardForUser_shouldSucceed_whenCreatingForSelf() throws Exception {
        // @PreAuthorize("...#userId.toString() == authentication.name")
        NewCardInfoDto cardDto = new NewCardInfoDto(
                "123",
                "Holder",
                LocalDate.now().plusYears(1));

        mockMvc.perform(post("/v1/users/" + selfUserId + "/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = selfUserId, roles = "USER")
    void createCardForUser_shouldFail_whenCreatingForOther() throws Exception {
        NewCardInfoDto cardDto = new NewCardInfoDto(
                "123",
                "Holder",
                LocalDate.now().plusYears(1));

        mockMvc.perform(post("/v1/users/" + otherUserId + "/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isForbidden());
    }


}