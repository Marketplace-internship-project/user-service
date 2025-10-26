package io.hohichh.marketplace.user.repository;

import io.hohichh.marketplace.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    //created for demonstration of method name query usage
    Optional<User> findByEmail(String email);

    //created for demonstration of jpql query usage
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findBySearchTerm(String searchTerm, Pageable pageable);

    //created for demonstration of native sql query usage
    @Query(value = "SELECT id, name, surname, birth_date, email FROM users u " +
            "WHERE EXTRACT(MONTH FROM u.birth_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(DAY FROM u.birth_date) = EXTRACT(DAY FROM CURRENT_DATE)",
            nativeQuery = true)
    List<User> findUsersWithBirthDayTodayNative();
}
