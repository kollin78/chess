package client;

import exception.ResponseException;
import model.LoginRequest;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setupServer() throws ResponseException {
        serverFacade.clearDB();
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void registerSuccess() throws ResponseException {
        var authData = serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));

        assertNotNull(authData);
    }

    @Test
    void registerFail() throws ResponseException {
        var authData = serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));

        assertThrows(ResponseException.class, () -> {
            serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));
        });
    }

    @Test
    void loginSuccess() throws ResponseException {
        serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));
        var authData = serverFacade.login(new LoginRequest("testPlayer", "realPassword"));

        assertNotNull(authData);
    }

    @Test
    void loginFail() throws ResponseException {
        serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));

        assertThrows(ResponseException.class, () -> {
           serverFacade.login(new LoginRequest("testPlayer2", "fakePassword"));
        });
    }

    @Test
    void logoutSuccess() throws ResponseException {
        var authData = serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));

        assertDoesNotThrow(() -> {
            serverFacade.logout(authData.authToken());
        });
    }

    @Test
    void logoutFail() throws ResponseException {
        var authData = serverFacade.register(new UserData("testPlayer", "realPassword", "test@byu.net"));

        assertThrows(ResponseException.class, () -> {
           serverFacade.logout("theRealSlimShady");
        });
    }
}
