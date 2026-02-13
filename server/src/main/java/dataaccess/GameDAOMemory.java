package dataaccess;

import model.GameData;
import chess.ChessGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GameDAOMemory implements GameDAO{
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int gameID = 1234;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        GameData data = new GameData(gameID, "", "", gameName, new ChessGame());
        games.put(gameID, data);
        gameID++;
        return gameID - 1; // returns gameID for this game, -1 bc you updated gameID before leaving function
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if(!games.containsKey(game.gameID())) {
            throw new DataAccessException("Error: Game Not Found");
        }

        games.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        gameID = 1234;
    }
}
