package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.LoginRequest;
import model.RegisterRequest;
import model.AuthResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public AuthResult registerUser(RegisterRequest req) throws DataAccessException {
        if((req.username() == null) || (req.username().isEmpty() || (req.password() == null) || (req.password().isEmpty()) || (req.email() == null) || (req.email().isEmpty()))) {
            throw new DataAccessException("Error: bad request");
        }

        if(userDAO.getUser(req.username()) != null) {
            throw new DataAccessException("Error: username already taken");
        }

        model.UserData newUser = new model.UserData(req.username(), req.password(), req.email());
        userDAO.createUser(newUser);

        String authToken = createAuth(req.username());

        return new AuthResult(req.username(), authToken);
    }

    public AuthResult login(LoginRequest req) throws DataAccessException {
        model.UserData user = userDAO.getUser(req.username());
        if((user == null) || (!user.password().equals(req.password()))) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = createAuth(req.username());

        return new AuthResult(req.username(), authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private String createAuth(String username) throws DataAccessException {
        String authToken = generateToken();
        model.AuthData authData = new model.AuthData(authToken, username);
        authDAO.createAuth(authData);

        return authToken;
    }
}
