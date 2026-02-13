package server;

import dataaccess.UserDAO;
import dataaccess.UserDAOMemory;
import io.javalin.*;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new UserDAOMemory();


    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
