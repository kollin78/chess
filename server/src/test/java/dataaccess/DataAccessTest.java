package dataaccess;

import model.AuthData;
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
        assertEquals(createdUser, retrievedUser);
    }

    @Test
    @DisplayName("get user epic fail")
    public void getUserFail() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "testPass", "test@byu.net"));

        assertThrows(DataAccessException.class, () -> dataAccess.getUser("fakeUser"));
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
}
