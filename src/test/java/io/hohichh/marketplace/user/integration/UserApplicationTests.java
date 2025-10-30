package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.AbstractApplicationTest;
import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.repository.CardRepository;
import io.hohichh.marketplace.user.repository.UserRepository;
import io.hohichh.marketplace.user.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;



class UserApplicationTests extends AbstractApplicationTest {

	@Test
	void testCreateUserAndGetById() {
		//--CREATE USER
		NewUserDto newUser = new NewUserDto(
				"Liza",
				"Hohich",
				LocalDate.of(1990, 5, 15),
				"liza.hohich@example.com"
		);

		ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(
				"/api/v1/users",
				newUser,
				UserDto.class
		);

		//--ASSERT
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		UserDto createdUser = createResponse.getBody();
		assertThat(createdUser).isNotNull();
		assertThat(createdUser.name()).isEqualTo("Liza");
		assertThat(createdUser.email()).isEqualTo("liza.hohich@example.com");
		UUID newUserId = createdUser.id();

		// --GET USER BY ID
		ResponseEntity<UserWithCardsDto> getResponse = restTemplate.getForEntity(
				"/api/v1/users/" + newUserId,
				UserWithCardsDto.class
		);

		// --ASSERT
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		UserWithCardsDto fetchedUser = getResponse.getBody();
		assertThat(fetchedUser).isNotNull();
		assertThat(fetchedUser.name()).isEqualTo("Liza");
		assertThat(fetchedUser.email()).isEqualTo("liza.hohich@example.com");
		assertThat(fetchedUser.cards()).isNotNull().isEmpty(); // Проверяем, что карты пустые
	}

	@Test
	void updateUser(){
		NewUserDto newUser = new NewUserDto("Alex",
				"MacNugget",
				LocalDate.of(2001, 10, 11),
				"MacAlex@gmail.com");

		ResponseEntity<UserDto> createdResponse = restTemplate.postForEntity(
				"api/v1/users",
				newUser,
				UserDto.class
		);

		UUID createdUserId = createdResponse.getBody().id();
		//тут будет еще код
	}

	@Test
	void deleteUser(){

	}

	@Test
	void getUserByEmail(){

	}

	@Test
	void getAllUsers(){

	}

	@Test
	void getAllUsersBySearchTerm(){

	}

	@Test
	void getAllUsersWithBirthdayToday(){

	}

	//-------CARD TEST SUITES




}