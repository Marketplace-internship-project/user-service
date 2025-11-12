package io.hohichh.marketplace.user.security;

import io.hohichh.marketplace.user.model.CardInfo;
import io.hohichh.marketplace.user.repository.CardRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component("userAndCardSecurity")
public class UserAndCardSecurity {

    private final CardRepository cardRepository;

    public UserAndCardSecurity(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public boolean isCardOwner(UUID cardId, Authentication authentication) {
        String principalId = authentication.getName();

        return cardRepository.findById(cardId)
                .map(card -> card.getUser().getId().toString().equals(principalId))
                .orElse(false);
    }
}