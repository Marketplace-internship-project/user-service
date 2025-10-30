package io.hohichh.marketplace.user;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
@EnableCaching
public class UserApplication {
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}
}
