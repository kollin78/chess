package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        userDAO = new UserDAOMemory();
        gameDAO = new GameDAOMemory();
        authDAO = new AuthDAOMemory();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    @DisplayName("clear success")
    public void clearSuccess() throws DataAccessException {
        userDAO.createUser(new model.UserData("test_username", "supersecretpassword", "realemail@byu.net"));
        authDAO.createAuth(new AuthData("realtokenforsure", "test_username"));
        gameDAO.createGame("test_game");

        clearService.clearAll();
        assertNull(userDAO.getUser("test_username"));
        assertNull(authDAO.getAuth("realtokenforsure"));
        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    @DisplayName("clear failure (womp womp)")
    public void clearFailure() throws DataAccessException {
        userDAO.createUser(new model.UserData("test_username", "supersecretpassword", "realemail@byu.net"));
        authDAO.createAuth(new AuthData("realtokenforsure", "test_username"));
        GameDAO failureOfAGameDAO = new GameDAOMemory() {
            @Override
            public void clear() throws DataAccessException {
                throw new DataAccessException("Error: (description of error)");
            }
        };

        clearService = new ClearService(userDAO, authDAO, failureOfAGameDAO);
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            clearService.clearAll();
        });

        assertTrue(e.getMessage().contains("error"));
    }

}
