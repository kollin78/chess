package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.google.gson.Gson;
import model.*;
import exception.ResponseException;

import static client.State.*;
import static com.sun.org.apache.xpath.internal.XPathAPI.eval;
import static ui.EscapeSequences.*;


public class ChessClient {

    private final ServerFacade serverFacade;
    private final String serverUrl;
    private State state = SIGNEDOUT;
    private AuthData authData = null;
    private ArrayList<GameData> gameList = new ArrayList<>();

    public ChessClient(String serverUrl) {
        serverFacade = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public void run() {
        var result = "";

        System.out.println("Welkomen to my chess server. Pls sign in to start");
        System.out.print(help());
        Scanner scanner = new Scanner(System.in);

        while(!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR);
            } catch (Throwable e){
                System.out.print(SET_TEXT_COLOR_RED + e.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    public String eval(String line) {
        try {
            String[] tokens = line.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch(cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "spectate" -> spectateGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String help() {
        if (state == SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                quit
                help
                """;
        }
        return """
            create <NAME>
            list
            join <ID> [WHITE or BLACK]
            spectate <ID>
            logout
            quit
            help
            """;
    }

    private void printPrompt() {
        String status;
        if(state == SIGNEDOUT) {
            status = "[SIGNED OUT]";
        } else {
            status = "[SIGNED IN]";
        }
        System.out.print("\n" + RESET_TEXT_COLOR + status + " >>> " + SET_TEXT_COLOR_GREEN);
    }

    private String register(String... params) throws ResponseException {
        if(params.length == 3) {
            var newUser = new UserData(params[0], params[1], params[2]);
            authData = serverFacade.register(newUser);
            state = SIGNEDIN;

            return String.format("Register successful. Now logged in as %s", authData.username());
        }
        throw new ResponseException(400, "Expected: <Username> <Password> <Email>, got sadness in return");
    }

    private String login(String... params) throws ResponseException {
        if(params.length == 2) {
            var request = new LoginRequest(params[0], params[1]);
            authData = serverFacade.login(request);
            state = SIGNEDIN;

            return String.format("Signed in as %s.", authData.username());
        }

        throw new ResponseException(400, "Expected: <Username> <Password>, got sadness in return");
    }

    private String logout() throws ResponseException {
        verifyLoggedIn();
        serverFacade.logout(authData.authToken());
        state = SIGNEDOUT;
        authData = null;
        return "You have signed out.";
    }

    private String createGame(String... params) throws ResponseException {
        verifyLoggedIn();
        if(params.length >= 1) {
            String gameName = String.join(" ", params);
            serverFacade.createGame(new CreateGameRequest(gameName), authData.authToken());

            return String.format("Game with name '%s' successfully created", gameName);
        }

        throw new ResponseException(400, "Expected: <GAME_NAME>, got sadness instead");
    }

    private String listGames() throws ResponseException {
        verifyLoggedIn();
        var gamesObject = serverFacade.listGames(authData.authToken()).games();
        gameList = new ArrayList<>(gamesObject);
        var gameString = new StringBuilder();
        for(int i = 0; i < gameList.size(); i++) {
            var game = gameList.get(i);
            gameString.append(String.format("%d. %s (W: %s, B: %s)\n",
                    i+1,
                    game.gameName(),
                    game.whiteUsername(),
                    game.blackUsername()));
        }

        return gameString.toString();
    }

    private String joinGame(String... params) throws ResponseException {
        verifyLoggedIn();
        if(params.length >= 2) {
            try {

            }
        }
    }

    private void verifyLoggedIn() throws ResponseException {
        if(state == SIGNEDOUT) {
            throw new ResponseException(401, "Pls sign in before trying anything else, thanks");
        }
    }
}
