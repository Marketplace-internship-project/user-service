package io.hohichh.marketplace.user.controller;

import io.hohichh.marketplace.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController("/api/v1")
public class RestUserController {
    private final UserService userService;

    public RestUserController(UserService userService){
        this.userService = userService;
    }

    //user methods======================================================================

    @PostMapping("/users")
    public void createUser(){}

    @PutMapping("/user/{id}")
    public void updateUser(){}

    @DeleteMapping("/user/{id}")
    public void deleteUser(){}

    @GetMapping("/user/{id}")
    public void getUserById(){}

    @GetMapping("/user/email={email}")
    public void getUserByEmail(){}

    @GetMapping("/users")
    public void getAllUsers(){}

    @GetMapping("/users/birthday=today")
    public void getUsersWithBirthdayToday(){}

    @GetMapping("/users/search={searchTerm}")
    public void getUsersBySearchTerm(){}


    //card methods======================================================================

    @PostMapping("/user/{user_id}/cards")
    public void createCardForUser(){}

    @DeleteMapping("/cards/{card_id}")
    public void deleteCard(){}

    @GetMapping("/cards/{card_id}")
    public void getCardById(){}

    @GetMapping("/cards/number={card_number}")
    public void getCardByNumber(){}

    @GetMapping("/user/{user_id}/cards")
    public void getCardsByUserId(){}

    @GetMapping("/cards/expired")
    public void getExpiredCards(){}
}
