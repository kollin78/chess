package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTest {

    private MySqlDataAccess dataAccess;

    @BeforeEach
    public void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

    @Test
    @DisplayName("clear test")
    public void clearTest() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net"));
        dataAccess.clear();

        assertNull(dataAccess.getUser("testUser"));
    }

    @Test
    @DisplayName("create user success")
    public void createUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net"));

        assertNotNull(dataAccess.getUser("testUser"));
    }

    @Test
    @DisplayName("create user epic fail")
    public void createUserFail() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net"));

        assertThrows(DataAccessException.class, () -> dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net")));
    }

    @Test
    @DisplayName("get user great success")
    public void getUserSuccess() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        UserData retrievedUser = dataAccess.getUser("testUser");

        assertEquals(createdUser.username(), retrievedUser.username());
        assertEquals(createdUser.email(), retrievedUser.email());
    }

    @Test
    @DisplayName("get user epic fail")
    public void getUserFail() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net"));

        assertNull(dataAccess.getUser("fakeUser"));
    }

    //createAuth, getAuth, deleteAuth

    @Test
    @DisplayName("create auth success")
    public void createAuthSuccess() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);

        assertNotNull(dataAccess.getAuth("realAuthToken"));
    }

    @Test
    @DisplayName("create auth epic fail")
    public void createAuthFail() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);

        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(new AuthData("realAuthToken", "testUser")));
    }

    @Test
    @DisplayName("get auth success")
    public void getAuthSuccess() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);

        assertEquals(testAuth, dataAccess.getAuth("realAuthToken"));
    }

    @Test
    @DisplayName("get auth epic fail")
    public void getAuthFail() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);

        assertNull(dataAccess.getAuth("randomToken"));
    }

    @Test
    @DisplayName("delete auth success")
    public void deleteAuthSuccess() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);
        dataAccess.deleteAuth("realAuthToken");

        assertNull(dataAccess.getAuth("realAuthToken"));
    }

    @Test
    @DisplayName("delete auth epic fail")
    public void deleteAuthFail() throws DataAccessException {
        UserData createdUser = new UserData("testUser", "testPass", "test@byu.net");
        dataAccess.createUser(createdUser);
        AuthData testAuth = new AuthData("realAuthToken", "testUser");
        dataAccess.createAuth(testAuth);
        dataAccess.deleteAuth("randomToken");


        assertNotNull(dataAccess.getAuth("realAuthToken"));
    }

    //createGame, getGame, listGames, updateGame

    @Test
    @DisplayName("create game success")
    public void createGameSuccess() throws DataAccessException {
        int gameID = dataAccess.createGame("realGame");

        assertNotNull(dataAccess.getGame(gameID));
    }

    @Test
    @DisplayName("create game fail")
    public void createGameFail() throws DataAccessException {

        assertThrows(DataAccessException.class, () -> dataAccess.createGame(null));
    }

    @Test
    @DisplayName("get game success")
    public void getGameSuccess() throws DataAccessException {
        int gameID = dataAccess.createGame("realGame");

        assertEquals("realGame", dataAccess.getGame(gameID).gameName());
    }

    @Test
    @DisplayName("get game fail")
    public void getGameFail() throws DataAccessException {

        assertNull(dataAccess.getGame(4321));
    }

    @Test
    @DisplayName("list game success")
    public void listGameSuccess() throws DataAccessException {
        GameData gameData = new GameData(1234, "whiteUsername", "blackUsername", "realGame", new ChessGame());
        dataAccess.createGame("realGame");

        assertNotNull(dataAccess.listGames());
    }

    @Test
    @DisplayName("list game fail")
    public void listGameFail() throws DataAccessException {

        assertTrue(dataAccess.listGames().isEmpty());
    }

    @Test
    @DisplayName("update game success")
    public void updateGameSuccess() throws DataAccessException {
        int gameID = dataAccess.createGame("createdGame");
        GameData realGame = new GameData(gameID, "whiteUsername", "blackUsername", "realGame", new ChessGame());
        dataAccess.updateGame(realGame);
        GameData retrievedGame = dataAccess.getGame(gameID);

        assertNotNull(retrievedGame);
        assertEquals("realGame", retrievedGame.gameName());
    }

    @Test
    @DisplayName("update game fail")
    public void updateGameFail() throws DataAccessException {
        int gameID = dataAccess.createGame("createdGame");
        GameData sadGame = new GameData(gameID, null, null, null, new ChessGame());

        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(sadGame));
    }
}
