package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

@SpringBootTest
public class InMemoryUserStorageTest {
    private final InMemoryUserStorage inMemoryUserStorrage;
    private User validUser;

    public InMemoryUserStorageTest() {
        this.inMemoryUserStorrage = new InMemoryUserStorage();
    }

    @BeforeEach
    void setUp() {
        this.validUser = new User(
                null,
                "valid@email.com",
                "validLogin",
                "Valid Name",
                LocalDate.of(2000, 10, 1)
        );
    }

    @Test
    void addValidUser() {
        User addedUser = inMemoryUserStorrage.addUser(validUser);
        assertNotNull(addedUser, "User is null");
        assertEquals(1, addedUser.getId());
        assertEquals("valid@email.com", addedUser.getEmail());
        assertEquals("validLogin", addedUser.getLogin());
        assertEquals("Valid Name", addedUser.getName());
        assertEquals(LocalDate.of(2000, 10, 1), addedUser.getBirthday());
    }

    @Test
    void updateUserSuccessfully() {
        User addedUser = inMemoryUserStorrage.addUser(validUser);
        User updatedUser = new User(
                addedUser.getId(),
                "update@email",
                "updatedLogin",
                "updated name",
                LocalDate.of(2002, 11, 11)
        );
        User result = inMemoryUserStorrage.updateUser(updatedUser);

        assertEquals(addedUser.getId(), result.getId());
        assertEquals(addedUser.getEmail(), result.getEmail());
        assertEquals(addedUser.getLogin(), result.getLogin());
        assertEquals(addedUser.getName(), result.getName());
        assertEquals(addedUser.getBirthday(), result.getBirthday());
    }

    @Test
    void getAllUsers() {
        inMemoryUserStorrage.addUser(validUser);
        Collection<User> users = inMemoryUserStorrage.getUsers();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("Valid Name", users.iterator().next().getName());
    }


}