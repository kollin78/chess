package service;

import dataaccess.*;
import model.AuthResult;
import model.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

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
}
