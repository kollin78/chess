package service;

import dataaccess.*;
import model.GameData;
import model.ListGamesResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService gameService;
    private String authToken;

    @BeforeEach
    public void setup() throws DataAccessException{
        gameDAO = new GameDAOMemory();
        authDAO = new AuthDAOMemory();
        gameService = new GameService(gameDAO, authDAO);
        authToken = "therealauthtoken";
        authDAO.createAuth(new model.AuthData(authToken, "test_username"));
    }

    // getGames, createGame, joinGame

    @Test
    @DisplayName("getGames success")
    public void getGamesSuccess() throws DataAccessException {
        Collection<GameData> result = gameService.getGames(authToken);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getGames epic fail")
    public void getGamesFail() throws DataAccessException {
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.getGames("nottherealauthtoken");
        });

        assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("createGame success")
    public void createGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame("test_game", authToken);
        assertTrue(gameID > 0);
        assertNotNull(gameDAO.getGame(gameID));
    }

    @Test
    @DisplayName("creatGame fail")
    public void createGameFail() throws DataAccessException {
       DataAccessException e = assertThrows(DataAccessException.class, () ->  {
           gameService.createGame("test_game", null);
       });

       assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("joinGame success")
    public void joinGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame("test_game", authToken);
        gameService.joinGame(authToken, "WHITE", gameID);
        assertEquals("test_username", gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    @DisplayName("joinGame fail")
    public void joinGameFail() throws DataAccessException {
        int gameID = gameService.createGame("test_game", authToken);
        DataAccessException e = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(null, "WHITE", gameID);
        });

        assertTrue(e.getMessage().contains("unauthorized"));
    }
}
