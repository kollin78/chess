package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import model.*;
import org.eclipse.jetty.server.Authentication;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.Collection;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new UserDAOMemory();
    private final AuthDAO authDAO = new AuthDAOMemory();
    private final GameDAO gameDAO = new GameDAOMemory();

    private final UserService userService = new UserService(userDAO, authDAO);
    private final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void clearHandler(Context context) {
        try {
            new ClearService(userDAO, authDAO, gameDAO).clearAll();
            context.status(200);
            context.result("{}");
        } catch (DataAccessException e) {
            handleError(e, context);
        }
    }

    private void registerHandler(Context context) {
        try {
            var req = new Gson().fromJson(context.body(), RegisterRequest.class);
            AuthResult result = userService.registerUser(req);
            context.status(200);
            context.result(new Gson().toJson(result));
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void loginHandler(Context context) {
        try {
            var req = new Gson().fromJson(context.body(), model.LoginRequest.class);
            AuthResult result = userService.login(req);
            context.status(200);
            context.result(new Gson().toJson(result));
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void logoutHandler(Context context) {
        try{
            String authToken = context.header("authorization");
            userService.logout(authToken);
            context.status(200);
            context.result("{}");
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void listGamesHandler(Context context) {
        try {
            String authToken = context.header("authorization");
            Collection<GameData> result = gameService.getGames(authToken);
            context.status(200);
            context.result(new Gson().toJson(new ListGamesResult(result)));
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void createGameHandler(Context context) {
        try {
            String authToken = context.header("authorization");
            var req = new Gson().fromJson(context.body(), CreateGameRequest.class);
            int gameID = gameService.createGame(req.gameName(), authToken);
            context.status(200);
            context.result(new Gson().toJson(new CreateGameResult(gameID)));
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void joinGameHandler(Context context) {
        try {
            String authToken = context.header("authorization");
            var req = new Gson().fromJson(context.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, req.playerColor(), req.gameID());
            context.status(200);
            context.result("{}");
        } catch(DataAccessException e) {
            handleError(e, context);
        }
    }

    private void handleError(DataAccessException e, Context context) {
        String message = e.getMessage();
        if (message.contains("bad request")) {
            context.status(400);
        } else if (message.contains("unauthorized")) {
            context.status(401);
        } else if (message.contains("already taken")) {
            context.status(403);
        } else {
            context.status(500);
        }
        context.result(new Gson().toJson(new ErrorResponse(message)));
    }
}
