package client;

import java.io.IOException;
import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.*;
import exception.ResponseException;
import websocket.messages.ServerMessage;

import static client.State.*;
import static ui.EscapeSequences.*;


public class ChessClient {

    private final ServerFacade serverFacade;
    private final String serverUrl;
    private State state = SIGNEDOUT;
    private AuthData authData = null;
    private ArrayList<GameData> gameList = new ArrayList<>();
    private ChessGame currentGame = null;
    private boolean isPlayerWhite = false;
    private WebSocketFacade webSocketFacade = null;
    private int currentGameID = -1;

    public ChessClient(String serverUrl) {
        serverFacade = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public void run() {
        var result = "";

        System.out.println("Welkomen to my chess server. Pls register or sign in to start");
        System.out.print(help());
        Scanner scanner = new Scanner(System.in);

        while(!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR);
            } catch (Throwable e){
                System.out.print(SET_TEXT_COLOR_RED + getErrorMessageFromJson(e.getMessage()) + RESET_TEXT_COLOR);
            }
        }
    }

    public String eval(String line) {
        try {
            String[] tokens = line.toLowerCase().split(" ");
            String cmd;
            if(tokens.length > 0) {
                cmd = tokens[0];
            } else {
                cmd = "help";
            }
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if(state == PLAYINGGAME) {
                return switch(cmd) {
                    case "redraw" -> DrawBoard.draw(currentGame.getBoard(), isPlayerWhite, null, null);
                    case "leave" -> leave();
                    case "move" -> makeMove(params);
                    case "resign" -> resign();
                    case "highlight" -> highlight(params);
                    default -> help();
                };
            }

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
            return getErrorMessageFromJson(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String help() {
        if(state == SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                quit
                help
                """;
        } else if(state == PLAYINGGAME) {
            return """
                redraw - redraw board from server
                leave - exit current game
                move <FROM> <TO> - makes move from <FROM> position to <TO> position
                resign - quit like a lil baby
                highlight <POSITION> - highlights valid moves for piece at <POSITION>
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
        String status = "";
        if(state == SIGNEDOUT) {
            status = "[SIGNED OUT]";
        } else if(state == SIGNEDIN) {
            status = "[SIGNED IN]";
        } else if(state == PLAYINGGAME) {
            status = "[PLAYING GAME]";
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
                var gamesObject = serverFacade.listGames(authData.authToken()).games();
                gameList = new ArrayList<>(gamesObject);
                int listNumber = Integer.parseInt(params[0]);
                String playerColor = params[1].toUpperCase();
                GameData selectedGame = gameList.get(listNumber - 1);
                currentGameID = selectedGame.gameID();

                if((!authData.username().equals(selectedGame.whiteUsername()) && (!authData.username().equals(selectedGame.blackUsername())))) {
                    serverFacade.joinGame(new JoinGameRequest(playerColor, selectedGame.gameID()), authData.authToken());
                }
                webSocketFacade = new WebSocketFacade(serverUrl, this);
                webSocketFacade.connect(authData.authToken(), currentGameID);
                state = PLAYINGGAME;
                isPlayerWhite = playerColor.equals("WHITE");



                return String.format("Successfully joined game as %s", playerColor);
            }
            catch (IndexOutOfBoundsException e) {
                throw new ResponseException(400, "Please enter a valid game number, thanks");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResponseException(400, "Expected: <gameID>, didn't get it :(");
    }

    private String spectateGame(String... params) throws ResponseException {
        verifyLoggedIn();
        if(params.length >= 1) {
            try {
                int listNumber = Integer.parseInt(params[0]);
                GameData selectedGame = gameList.get(listNumber - 1);
                currentGameID = selectedGame.gameID();
                isPlayerWhite = true;
                state = PLAYINGGAME;

                webSocketFacade = new WebSocketFacade(serverUrl, this);
                webSocketFacade.connect(authData.authToken(), currentGameID);

                return "Spectating game";
            } catch (IndexOutOfBoundsException e) {
                throw new ResponseException(400, "Game number doesn't exist, please pick a valid game number from listGames");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new ResponseException(400, "Expected: <gameID>, got something that was not <gameID>");
    }

    private String highlight(String... params) throws ResponseException {
        if(params.length < 1) {
            throw new ResponseException(400, "Expected: highlight <Position>, got something that was probably not a valid position...");
        }

        ChessPosition chessPosition = getPositionFromInput(params[0]);
        Collection<ChessMove> validMoves = currentGame.validMoves(chessPosition);
        if((validMoves == null) || validMoves.isEmpty()) {
            return "No legal moves for selected piece... good luck brother";
        }

        return DrawBoard.draw(currentGame.getBoard(), isPlayerWhite, validMoves, chessPosition);
    }

    private String makeMove(String... params) throws ResponseException, IOException {
        if(params.length < 2) {
            throw new ResponseException(400, "Expected: makeMove <FROM> <TO>, but probably got invalid positions");
        }

        ChessPosition fromPosition = getPositionFromInput(params[0]);
        ChessPosition toPosition = getPositionFromInput(params[1]);
        ChessMove newMove = new ChessMove(fromPosition, toPosition, null);

        webSocketFacade.makeMove(authData.authToken(), currentGameID, newMove);

        return "Move has been made, hopefully it was a good one";
    }

    private String resign() throws ResponseException, IOException {
        System.out.println(SET_TEXT_COLOR_RED + "Are you sure you want to quit and let everyone know you are a baby? (y/n)" + SET_TEXT_COLOR_WHITE);
        String quitterInput = new Scanner(System.in).nextLine().toLowerCase();

        if(quitterInput.equals("y")) {
            webSocketFacade.resign(authData.authToken(), currentGameID);
            return "Resigning like a lil baby";
        }
        return "Canceling resignation, good choice";
    }

    private String leave() throws ResponseException, IOException {
        webSocketFacade.leave(authData.authToken(), currentGameID);
        state = SIGNEDIN;
        webSocketFacade = null;
        return "Leaving game, pls join again soon :)";
    }

    private ChessPosition getPositionFromInput(String input) {
        int column = (input.toLowerCase().charAt(0) - 'a' + 1);
        int row = Character.getNumericValue(input.charAt(1));

        return new ChessPosition(row, column);
    }

    private String getErrorMessageFromJson(String jsonError) {
        try {
            if(jsonError != null) {
                int startInd = jsonError.indexOf('{');
                if(startInd != -1) {
                    String stringError = jsonError.substring(startInd);
                    var map = new Gson().fromJson(stringError, Map.class);
                    if(map.containsKey("message")) {
                        return map.get("message").toString();
                    }
                }
            }
        } catch(Exception e) {
            return "An unknown error occurred";
        }

        return jsonError;
    }

    private void verifyLoggedIn() throws ResponseException {
        if(state == SIGNEDOUT) {
            throw new ResponseException(401, "Pls sign in before trying anything else, thanks");
        }
    }

    public void messageHandler(ServerMessage serverMessage) {
        switch(serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
                this.currentGame = serverMessage.getGame();
                System.out.println("\n" + DrawBoard.draw(currentGame.getBoard(), isPlayerWhite, null, null));
                printPrompt();
            } case NOTIFICATION -> {
                System.out.println("\n" + DrawBoard.draw(currentGame.getBoard(), isPlayerWhite, null, null));
                System.out.println(SET_TEXT_COLOR_MAGENTA + "\n" + serverMessage.getMessage());
                printPrompt();
            } case ERROR -> {
                System.out.println(SET_TEXT_COLOR_RED + "\n\n" + serverMessage.getErrorMessage());
                printPrompt();
            }
        }
    }
}
