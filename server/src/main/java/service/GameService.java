package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Collection<GameData> getGames(String authToken) throws DataAccessException {
        verifyAuth(authToken);

        return gameDAO.listGames();
    }

    public int createGame(String gameName, String authToken) throws DataAccessException {
        verifyAuth(authToken);
        if((gameName == null) || (gameName.isEmpty())) {
            throw new DataAccessException("Error: bad request");
        }

        return gameDAO.createGame(gameName);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        verifyAuth(authToken);
        GameData gameData = gameDAO.getGame(gameID);

        if(gameData == null) {
            throw new DataAccessException("Error: bad request");
        }
        if((playerColor == null) || (playerColor.isEmpty())) {
            throw new DataAccessException("Error: bad request");
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        String currentUser = authDAO.getAuth(authToken).username();

        if(playerColor.equalsIgnoreCase("WHITE")) {
            if((whiteUser != null) && (!whiteUser.isEmpty())) {
                throw new DataAccessException("Error: already taken");
            }
            whiteUser = currentUser;
        } else if(playerColor.equalsIgnoreCase("BLACK")) {
            if((blackUser != null) && (!blackUser.isEmpty())) {
                throw new DataAccessException("Error: already taken");
            }
            blackUser = currentUser;
        } else {
            throw new DataAccessException("Error: bad request");
        }

        gameDAO.updateGame(new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game()));
    }

    private void verifyAuth(String authToken) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if(authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

}
