package service;

import dataaccess.*;
import model.AuthResult;
import model.LoginRequest;
import model.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new UserDAOMemory();
        authDAO = new AuthDAOMemory();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("successful register")
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("test_username", "supersecretpassword", "realemail@byu.net");
        AuthResult result = userService.registerUser(request);

        assertNotNull(result.authToken());
        assertEquals("test_username", result.username());
    }

    @Test
    @DisplayName("fail to launch(register)")
    public void registerFail() throws DataAccessException {
        RegisterRequest request = new RegisterRequest(null, "supersecretpassword", "realemail@byu.net");
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            userService.registerUser(request);
        });

        assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("successful login")
    public void loginSuccess() throws DataAccessException {
        userService.registerUser(new RegisterRequest("test_username", "supersecretpassword", "email@byu.net"));
        LoginRequest request = new LoginRequest("test_username", "supersecretpassword");
        AuthResult result = userService.login(request);

        assertNotNull(result.authToken());
        assertEquals("test_username", result.username());
    }

    @Test
    @DisplayName("fail to login")
    public void loginFail() throws DataAccessException {
        userService.registerUser(new RegisterRequest("test_username", "supersecretpassword", "email@byu.net"));
        LoginRequest request = new LoginRequest("test_username", null);
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            userService.login(request);
        });

        assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("successful logout")
    public void logoutSuccess() throws DataAccessException {
        AuthResult auth = userService.registerUser(new RegisterRequest("test_username", "supersecretpassword", "email@byu.net"));
        assertDoesNotThrow(() -> userService.logout(auth.authToken()));
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("fail to logout")
    public void logoutFail() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            userService.logout("unauthorized");
        });
    }
}
