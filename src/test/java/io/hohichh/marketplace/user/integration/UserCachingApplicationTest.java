package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.AbstractApplicationTest;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.exception.GlobalExceptionHandler;
import io.hohichh.marketplace.user.repository.CardRepository;
import io.hohichh.marketplace.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserCachingApplicationTest extends AbstractApplicationTest {


    @Test
    void testCacheEvictionOnUpdate() {
        //--ARRANGE: CREATE USERS
        NewUserDto newUser = new NewUserDto("Cache", "Test", null, "cache@test.com");
        UserDto createdUser = restTemplate.postForEntity("/api/v1/users", newUser, UserDto.class).getBody();
        UUID userId = createdUser.id();

        //--ACT 1: GET USER (MUST STORED IN CACHE NOW)
        restTemplate.getForEntity("/api/v1/users/" + userId, UserWithCardsDto.class);

        //--ACT 2: UPDATE USER (MUST EVICT CACHE)
        NewUserDto updatedUserDto = new NewUserDto("Cache", "Updated", null, "cache-updated@test.com");
        restTemplate.put("/api/v1/users/" + userId, updatedUserDto);

        //--ACT 3: DELETE FROM DB (TO CHECK IF THE CACHE DIDN'T EVICTED)
        userRepository.deleteById(userId);

        //--ACT 4: GET DELETED USER (MUST THROW EXCEPTION IF CACHE HAS EVICTED)
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/users/" + userId,
                GlobalExceptionHandler.ErrorResponse.class
        );

        //--ASSERT
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody().message()).contains("User with id " + userId + " not found.");
    }

}
