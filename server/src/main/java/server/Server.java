package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import model.AuthResult;
import model.ErrorResponse;
import model.GameData;
import model.RegisterRequest;
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
        javalin.get("/game", this::listGameHandler);
        javalin.post("/game", this::createGameHandler);

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
            context.status(500);
            context.json(new ErrorResponse(e.getMessage()));
        }
    }

    private void registerHandler(Context context) {
        try {
            var req = new Gson().fromJson(context.body(), RegisterRequest.class);
            AuthResult result = userService.registerUser(req);
            context.status(200);
            context.result(new Gson().toJson(result));
        } catch(DataAccessException e) {
            if(e.getMessage().contains("bad request")) {
                context.status(400);
            } else if (e.getMessage().contains("username already taken")) {
                context.status(403);
            } else {
                context.status(500);
            }

            context.result(new Gson().toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void loginHandler(Context context) {
        try {
            var req = new Gson().fromJson(context.body(), model.LoginRequest.class);
            AuthResult result = userService.login(req);
            context.status(200);
            context.result(new Gson().toJson(result));
        } catch(DataAccessException e) {
            if(e.getMessage().contains("bad request")) {
                context.status(400);
            } else if(e.getMessage().contains("unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }

            context.result(new Gson().toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void logoutHandler(Context context) {
        try{
            String authToken = context.header("authorization");
            userService.logout(authToken);
            context.status(200);
            context.result("{}");
        } catch(DataAccessException e) {
            if(e.getMessage().contains("unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }

            context.result(new Gson().toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void listGameHandler(Context context) {
        try {
            String authToken = context.header("authorization");
            Collection<GameData> result = gameService.getGames(authToken);
            context.status(200);
            context.result(new Gson().toJson(result));
        } catch(DataAccessException e) {
            if(e.getMessage().contains("unauthorized")) {
                context.status(401);
            } else {
                context.status(500);
            }

            context.result(new Gson().toJson(new ErrorResponse(e.getMessage())));
        }
    }

    private void createGamehandler(Context context) {
        try {
            String authToken = context.header("authorization");
            var req = new Gson().fromJson(context.body());

        } catch(DataAccessException e) {

        }
    }
}
