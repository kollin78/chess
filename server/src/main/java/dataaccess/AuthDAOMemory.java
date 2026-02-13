package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class AuthDAOMemory implements AuthDAO {
    private final HashMap<String, AuthData> authDatas = new HashMap<>();

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        authDatas.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDatas.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDatas.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        authDatas.clear();
    }
}
