package server;

import dataaccess.*;
import io.javalin.*;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new UserDAOMemory();
    private final AuthDAO authDAO = new AuthDAOMemory();
    private final GameDAO gameDAO = new GameDAOMemory();


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        javalin.delete("/db", (req) -> {
            try {
                new ClearService(userDAO, authDAO, gameDAO).clearAll();
                req.status(200);
                req.result();
            } catch (DataAccessException e) {
                req.status(500);
                req.result("{\"message\": \"Error: " + e.getMessage() + "\" }");
            }
        });
        javalin.post("/user", (req) -> {
            new
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
