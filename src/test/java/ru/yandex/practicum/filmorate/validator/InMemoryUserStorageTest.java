package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
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

//    @Test
//    void addNullUser() {
//        ValidationException exception = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(null);
//        });
//        assertEquals("User is null", exception.getMessage());
//    }
//
//    @Test
//    void addUserWithInvalidEmail() {
//        User invalidUser = new User(
//                null,
//                null,
//                "Login",
//                "Name",
//                LocalDate.of(2000, 10, 1)
//        );
//        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Email cannot be empty and shell contain at least one @", exception1.getMessage());
//
//        invalidUser.setEmail("email");
//        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Email cannot be empty and shell contain at least one @", exception2.getMessage());
//
//        invalidUser.setLogin("   ");
//        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Email cannot be empty and shell contain at least one @", exception3.getMessage());
//    }
//
//    @Test
//    void addUserWithInvalidLogin() {
//        User invalidUser = new User(
//                null,
//                "valid@mail.com",
//                null,
//                "Name",
//                LocalDate.of(2000, 10, 1)
//        );
//        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Login cannot be empty and shell not contain spaces", exception1.getMessage());
//
//        invalidUser.setLogin("   ");
//        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Login cannot be empty and shell not contain spaces", exception2.getMessage());
//
//        invalidUser.setLogin("Login with spaces");
//        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Login cannot be empty and shell not contain spaces", exception3.getMessage());
//    }
//
//    @Test
//    void addUserWithBirthdayInFuture() {
//        User invalidUser = new User(
//                null,
//                "valid@mail.com",
//                "validLogin",
//                "Name",
//                LocalDate.of(2050, 10, 1)
//        );
//        ValidationException exception = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.addUser(invalidUser);
//        });
//        assertEquals("Birthday cannot be in future", exception.getMessage());
//    }
//
//    @Test
//    void nullableNameShellBeEqualisedToLogin() {
//        User noNameUser = new User(
//                null,
//                "valid@mail.com",
//                "validLogin",
//                null,
//                LocalDate.of(2000, 10, 1)
//        );
//
//        User resultUser = inMemoryUserStorrage.addUser(noNameUser);
//        assertEquals("validLogin", resultUser.getName());
//    }

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

//    @Test
//    void updateUserWithInvalidId() {
//        User userWithInvalidId = new User(
//                999,
//                "update@email",
//                "updatedLogin",
//                "updated name",
//                LocalDate.of(2002, 11, 11)
//        );
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            inMemoryUserStorrage.updateUser(userWithInvalidId);
//        });
//        assertEquals("user with id = " + userWithInvalidId.getId() + " not found", exception.getMessage());
//    }
//
//    @Test
//    void updateUserWithNullId() {
//        User userWithNullId = new User(
//                null,
//                "update@email",
//                "updatedLogin",
//                "updated name",
//                LocalDate.of(2002, 11, 11)
//        );
//        ValidationException exception = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.updateUser(userWithNullId);
//        });
//        assertEquals("id is null", exception.getMessage());
//    }
//
//    @Test
//    void updateUserWithNull() {
//        ValidationException exception = assertThrows(ValidationException.class, () -> {
//            inMemoryUserStorrage.updateUser(null);
//        });
//        assertEquals("Updated user is null", exception.getMessage());
//
//    }

    @Test
    void getAllUsers() {
        inMemoryUserStorrage.addUser(validUser);
        Collection<User> users = inMemoryUserStorrage.getUsers();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("Valid Name", users.iterator().next().getName());
    }


}