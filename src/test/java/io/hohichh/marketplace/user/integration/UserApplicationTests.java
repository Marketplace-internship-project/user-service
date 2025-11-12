package io.hohichh.marketplace.user.integration;

import io.hohichh.marketplace.user.dto.NewUserDto;
import io.hohichh.marketplace.user.dto.UserDto;
import io.hohichh.marketplace.user.dto.UserWithCardsDto;
import io.hohichh.marketplace.user.integration.config.TestClockConfiguration;
import io.hohichh.marketplace.user.integration.config.TestContainerConfiguration;
import io.hohichh.marketplace.user.model.User;
import io.hohichh.marketplace.user.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({
		TestClockConfiguration.class,
		TestContainerConfiguration.class
})
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
class UserApplicationTests extends AbstractApplicationTest {

	private NewUserDto testUser;

	@BeforeEach
	void initTestUserData(){
		testUser = new NewUserDto(
				"Adam",
				"FirstHuman",
				LocalDate.of(1999, 1,1),
				"AdamHuman@Gmail.com"
		);
	}

	@AfterEach
	void tearDown() {
		//clear redis cache
		cacheManager.getCacheNames().stream()
				.map(cacheManager::getCache)
				.filter(java.util.Objects::nonNull)
				.forEach(org.springframework.cache.Cache::clear);

		//clear postgres database
		userRepository.deleteAll();
	}


	private UserDto createTestUser() {
		ResponseEntity<UserDto> response = restTemplate.postForEntity(
				"/v1/users",
				testUser,
				UserDto.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}


	@Test
	void createUser_shouldReturnCreatedUser_withCreatedStatus() {
		ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(
				"/v1/users",
				testUser,
				UserDto.class
		);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		UserDto createdUser = createResponse.getBody();
		assertThat(createdUser).isNotNull();
		assertThat(createdUser.name()).isEqualTo("Adam");
		assertThat(createdUser.email()).isEqualTo("AdamHuman@Gmail.com");

		User savedUser = userRepository.findById(createdUser.id()).orElse(null);
		assertThat(savedUser).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo("AdamHuman@Gmail.com");
	}

	@Test
	void createUser_ShouldReturnConflictStatus_whenEmailAlreadyExists(){
		UserDto firstUser = createTestUser();

		NewUserDto duplicateUserDto = new NewUserDto(
				"Eve",
				"SecondHuman",
				LocalDate.of(2000, 1, 1),
				firstUser.email() // Используем email первого юзера
		);

		ResponseEntity<GlobalExceptionHandler.ErrorResponse> conflictResponse = restTemplate.postForEntity(
				"/v1/users",
				duplicateUserDto,
				GlobalExceptionHandler.ErrorResponse.class
		);

		assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		assertThat(conflictResponse.getBody()).isNotNull();
		assertThat(conflictResponse.getBody().message())
				.isEqualTo("User with email " + firstUser.email() + " already exists.");
	}

	@Test
	void updateUser_shouldReturnUpdatedUser_withOkStatus(){
		UserDto createdUser = createTestUser(); // Используем helper

		NewUserDto updateUserData = new NewUserDto(
				"Eve",
				"FirstHuman",
				LocalDate.of(2001, 10, 11),
				"AppleGirl@gmail.com"
		);
		HttpEntity<NewUserDto> requestEntity = new HttpEntity<>(updateUserData);

		ResponseEntity<UserDto> updatedUser = restTemplate.exchange(
				"/v1/users/" + createdUser.id(),
				HttpMethod.PUT,
				requestEntity,
				UserDto.class
		);

		assertThat(updatedUser.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(updatedUser.getBody()).isNotNull();
		assertThat(updatedUser.getBody().id()).isEqualTo(createdUser.id());
		assertThat(updatedUser.getBody().name()).isEqualTo("Eve");
		assertThat(updatedUser.getBody().surname()).isEqualTo("FirstHuman");
		assertThat(updatedUser.getBody().email()).isEqualTo("AppleGirl@gmail.com");
	}

	@Test
	void updateUser_ShouldReturnNotFoundStatus_whenUserIdDoesntExist(){
		UUID nonexistentUserId = UUID.randomUUID();

		NewUserDto updateUserData = new NewUserDto(
				"Ghost",
				"User",
				LocalDate.of(2000, 1, 1),
				"ghost@example.com"
		);
		HttpEntity<NewUserDto> requestEntity = new HttpEntity<>(updateUserData);


		ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = restTemplate.exchange(
				"/v1/users/" + nonexistentUserId,
				HttpMethod.PUT,
				requestEntity,
				GlobalExceptionHandler.ErrorResponse.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().message())
				.isEqualTo("User with id " + nonexistentUserId + " not found.");
	}

	@Test
	void deleteUser_ShouldReturnNoContent_WhenUserDeleted(){
		UserDto createdUser = createTestUser(); // Используем helper
		UUID userId = createdUser.id();

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"/v1/users/" + userId,
				HttpMethod.DELETE,
				null,
				Void.class
		);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(deleteResponse.getBody()).isNull();
		assertThat(userRepository.findById(userId)).isEmpty();
	}

	@Test
	void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist(){
		UUID nonexistentUserId = UUID.randomUUID();

		ResponseEntity<GlobalExceptionHandler.ErrorResponse> deleteResponse = restTemplate.exchange(
				"/v1/users/" + nonexistentUserId,
				HttpMethod.DELETE,
				null,
				GlobalExceptionHandler.ErrorResponse.class
		);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(deleteResponse.getBody().message())
				.isEqualTo("User with id " + nonexistentUserId + " not found.");
	}

	@Test
	void getUserById(){
		UserDto createdUser = createTestUser();
		UUID newUserId = createdUser.id();

		ResponseEntity<UserWithCardsDto> getResponse = restTemplate.getForEntity(
				"/v1/users/" + newUserId,
				UserWithCardsDto.class
		);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		UserWithCardsDto fetchedUser = getResponse.getBody();
		assertThat(fetchedUser).isNotNull();
		assertThat(fetchedUser.name()).isEqualTo("Adam");
		assertThat(fetchedUser.email()).isEqualTo("AdamHuman@Gmail.com");
		assertThat(fetchedUser.cards()).isNotNull().isEmpty();
	}

	@Test
	void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() {
		UUID nonexistentUserId = UUID.randomUUID();

		ResponseEntity<GlobalExceptionHandler.ErrorResponse> getResponse = restTemplate.getForEntity(
				"/v1/users/" + nonexistentUserId,
				GlobalExceptionHandler.ErrorResponse.class
		);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(getResponse.getBody().message())
				.isEqualTo("User with id " + nonexistentUserId + " not found.");
	}

	@Test
	void getUserByEmail(){
		UserDto createdUser = createTestUser();

		String url = "/v1/users?email={emailParam}";
		ResponseEntity<UserWithCardsDto> emailResponse = restTemplate.getForEntity(
				url,
				UserWithCardsDto.class,
				createdUser.email()
		);

		assertThat(emailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		UserWithCardsDto fetchedUser = emailResponse.getBody();
		assertThat(fetchedUser).isNotNull();
		assertThat(fetchedUser.id()).isEqualTo(createdUser.id());
		assertThat(fetchedUser.email()).isEqualTo(createdUser.email());
		assertThat(fetchedUser.cards()).isNotNull().isEmpty();
	}

	@Test
	void getUserByEmail_ShouldReturnNotFound_WhenEmailDoesNotExist() {
		String nonexistentEmail = "no-one@example.com";
		String url = "/v1/users?email={emailParam}";

		ResponseEntity<UserWithCardsDto> emailResponse = restTemplate.getForEntity(
				url,
				UserWithCardsDto.class,
				nonexistentEmail
		);

		assertThat(emailResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getAllUsers_shouldReturnPaginatedListOfUsers() {
		createTestUser();

		NewUserDto user2 = new NewUserDto(
				"Eve",
				"SecondHuman",
				LocalDate.of(2000, 1, 1),
				"EveHuman@Gmail.com"
		);

		restTemplate.postForEntity("/v1/users", user2, UserDto.class);

		ParameterizedTypeReference<RestResponsePage<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};

		ResponseEntity<RestResponsePage<UserDto>> response = restTemplate.exchange(
				"/v1/users", HttpMethod.GET, null, responseType
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		RestResponsePage<UserDto> responseBody = response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getTotalElements()).isEqualTo(2);
		assertThat(responseBody.getContent())
				.extracting(UserDto::email)
				.containsExactlyInAnyOrder("AdamHuman@Gmail.com", "EveHuman@Gmail.com");
	}

	@Test
	void getAllUsers_shouldReturnEmptyPage_whenNoUsersExist() {
		ParameterizedTypeReference<RestResponsePage<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};

		ResponseEntity<RestResponsePage<UserDto>> response = restTemplate.exchange(
				"/v1/users", HttpMethod.GET, null, responseType
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		RestResponsePage<UserDto> responseBody = response.getBody();
		assertThat(responseBody).isNotNull();
		assertThat(responseBody.getTotalElements()).isEqualTo(0);
	}

	@Test
	void getAllUsersBySearchTerm(){
		createTestUser();

		NewUserDto user2 = new NewUserDto(
				"Eve",
				"SecondHuman",
				LocalDate.of(2000, 1, 1),
				"EveHuman@Gmail.com"
		);
		restTemplate.postForEntity("/v1/users", user2, UserDto.class);

		NewUserDto user3 = new NewUserDto(
				"Robert",
				"Smith",
				LocalDate.of(1985, 5, 5),
				"r.smith@company.com"
		);
		restTemplate.postForEntity("/v1/users", user3, UserDto.class);


		ParameterizedTypeReference<RestResponsePage<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};
		String url = "/v1/users?search={searchTerm}";


		ResponseEntity<RestResponsePage<UserDto>> responseHuman = restTemplate.exchange(
				url, HttpMethod.GET, null, responseType, "Human"
		);
		assertThat(responseHuman.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseHuman.getBody().getTotalElements()).isEqualTo(2);


		ResponseEntity<RestResponsePage<UserDto>> responseSmith = restTemplate.exchange(
				url, HttpMethod.GET, null, responseType, "smith"
		);
		assertThat(responseSmith.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseSmith.getBody().getTotalElements()).isEqualTo(1);


		ResponseEntity<RestResponsePage<UserDto>> responseZebra = restTemplate.exchange(
				url, HttpMethod.GET, null, responseType, "Zebra"
		);
		assertThat(responseZebra.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseZebra.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	void getAllUsersBySearchTerm_ShouldReturnEmptyPage(){
		createTestUser();

		ParameterizedTypeReference<RestResponsePage<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};
		String url = "/v1/users?search={searchTerm}";
		String searchTerm = "ZebraNonExistentTerm";

		ResponseEntity<RestResponsePage<UserDto>> response = restTemplate.exchange(
				url, HttpMethod.GET, null, responseType, searchTerm
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}

	@Test
	void getAllUsersWithBirthdayToday_ShouldReturnUserList(){
		final LocalDate MOCKED_TODAY = LocalDate.of(2025, 10, 30);
		Instant fixedInstant = MOCKED_TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

		NewUserDto userToday1 = new NewUserDto("Mike", "Today",
				MOCKED_TODAY.minusYears(20), "mike@today.com");
		restTemplate.postForEntity("/v1/users", userToday1, UserDto.class);

		NewUserDto userToday2 = new NewUserDto("Sarah", "Present",
				MOCKED_TODAY.minusYears(30), "sarah@present.com");
		restTemplate.postForEntity("/v1/users", userToday2, UserDto.class);

		NewUserDto userTomorrow = new NewUserDto("Tom", "Morrow",
				MOCKED_TODAY.plusDays(1).minusYears(25), "tom@morrow.com");
		restTemplate.postForEntity("/v1/users", userTomorrow, UserDto.class);

		createTestUser(); // Adam, 1999-01-01

		ParameterizedTypeReference<List<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};

		ResponseEntity<List<UserDto>> response = restTemplate.exchange(
				"/v1/users?birth-date=today", HttpMethod.GET, null, responseType
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<UserDto> birthdayUsers = response.getBody();
		assertThat(birthdayUsers).isNotNull();
		assertThat(birthdayUsers).hasSize(2);
		assertThat(birthdayUsers)
				.extracting(UserDto::email)
				.containsExactlyInAnyOrder("mike@today.com", "sarah@present.com");
	}

	@Test
	void getAllUsersWithBirthdayToday_shouldReturnEmptyList_whenNoBirthdaysMatch() {
		final LocalDate MOCKED_TODAY = LocalDate.of(2025, 10, 30);
		Instant fixedInstant = MOCKED_TODAY.atStartOfDay(ZoneId.of("UTC")).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

		createTestUser(); // Adam, 1999-01-01

		ParameterizedTypeReference<List<UserDto>> responseType =
				new ParameterizedTypeReference<>() {};

		ResponseEntity<List<UserDto>> response = restTemplate.exchange(
				"/v1/users?birth-date=today", HttpMethod.GET, null, responseType
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull().isEmpty();
	}
}