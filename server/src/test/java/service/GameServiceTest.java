package service;

import dataaccess.*;
import model.ListGamesResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GameServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setup() {
        gameDAO = new GameDAOMemory();
        authDAO = new AuthDAOMemory();
    }

    // getGames, createGames, joinGame

    @Test
    @DisplayName("getGames success")
    public void getGamesSuccess() throws DataAccessException {
        ListGamesResult listGamesResult = new ListGamesResult()
    }
}
