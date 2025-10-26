package io.hohichh.marketplace.user.repository;

import io.hohichh.marketplace.user.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<CardInfo, UUID> {
    //created for demonstration of method name query usage
    Optional<CardInfo> findByNumber(String number);

    //created for demonstration of jpql query usage
    @Query("SELECT c FROM CardInfo c WHERE c.user.id = :userId")
    List<CardInfo> findByUserId(UUID userId);

    //created for demonstration of native sql query usage
    @Query(value = "SELECT id, user_id, \"number\", holder, expiration_date " +
            "FROM card_info " +
            "WHERE expiration_date < CURRENT_DATE",
            nativeQuery = true)
    List<CardInfo> findExpiredCardsNative();
}
