package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler{
    private final ConnectionManager connectionManager = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WsRequestHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connectionManager.remove(ctx.session);
        System.out.println("Disconnecting");
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Connected to: " + ctx.sessionId());
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            String jsonMsg = ctx.message();
            UserGameCommand userGameCommand = new Gson().fromJson(jsonMsg, UserGameCommand.class);

            switch(userGameCommand.getCommandType()) {
                case CONNECT -> doConnect(userGameCommand, ctx);
                case MAKE_MOVE -> makeMove(jsonMsg, ctx);
                case LEAVE -> leave(userGameCommand, ctx);
                case RESIGN -> resign(userGameCommand, ctx);
                default -> System.out.println("Please enter a valid command");
            }
        } catch(Exception e) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: an error occurred, amigo (in handleMessage)\n " + e.getMessage());
            ctx.send(new Gson().toJson(errorMessage));
        }
    }

    private void doConnect(UserGameCommand userGameCommand, WsMessageContext ctx) {
        try {
            AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
            if(authData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: Unauthorized, pal");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }
            GameData gameData = gameDAO.getGame(userGameCommand.getGameID());
            if(gameData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: invalid game data, buddy");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }

            String username = authData.username();
            int gameID = userGameCommand.getGameID();
            connectionManager.add(gameID, username, ctx.session);

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(gameData);
            ctx.send(new Gson().toJson(loadGame));

            String playerColor = "observer";
            if(username.equals(gameData.whiteUsername())) {
                playerColor = "white player";
            } else if(username.equals(gameData.blackUsername())) {
                playerColor = "black player";
            }

            String message = (username + " has joined the game as " + playerColor + ", hold on to your booty.");
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(message);
            connectionManager.broadcast(gameID, ctx.session, notification);
        } catch(DataAccessException e) {

            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: " + e.getMessage());
            ctx.send(new Gson().toJson(errorMessage));
        } catch (IOException e) {
            //intellij said we needed this
            throw new RuntimeException(e);
        }
    }

    private void makeMove(String jsonMsg, WsMessageContext ctx) throws Exception {
        UserGameCommand userGameCommand = new Gson().fromJson(jsonMsg, UserGameCommand.class);
        AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
        if(authData == null) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: unauthorized, bub");
            ctx.send(new Gson().toJson(errorMessage));
            return;
        }
        GameData gameData = gameDAO.getGame(userGameCommand.getGameID());
        if(gameData == null) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: you got no game data, friend");
            ctx.send(new Gson().toJson(errorMessage));
            return;
        }
        ChessGame chessGame = gameData.game();
        if(chessGame.isGameFinished()) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: how are you expecting to make a move in a game that is finished?");
            ctx.send(new Gson().toJson(errorMessage));
            return;
        }

        String username = authData.username();
        boolean isWhite = false;
        boolean isBlack = false;
        if(username.equals(gameData.whiteUsername())) {
            isWhite = true;
        } else if(username.equals(gameData.blackUsername())) {
            isBlack = true;
        }

        if((isWhite && (chessGame.getTeamTurn() != ChessGame.TeamColor.WHITE)) || (isBlack && (chessGame.getTeamTurn() != ChessGame.TeamColor.BLACK))) {
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: wait your turn, bud");
            ctx.send(new Gson().toJson(errorMessage));
            return;
        } else if ((!isWhite) && (!isBlack)) {
            //spectator
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: you're supposed to just be watching, not making moves... chill bro");
            ctx.send(new Gson().toJson(errorMessage));
            return;
        }

        try {
            JsonObject jsonMove = new Gson().fromJson(jsonMsg, JsonObject.class);
            ChessMove normalMove = new Gson().fromJson(jsonMove.get("move"), ChessMove.class);
            ChessPiece movePiece = gameData.game().getBoard().getPiece(normalMove.getStartPosition());
            chessGame.makeMove(normalMove);

            GameData updatedGameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    chessGame
            );
            gameDAO.updateGame(updatedGameData);

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(updatedGameData);
            connectionManager.broadcast(gameData.gameID(), null, loadGame);

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            String pieceType = "";
            if(movePiece != null) {
                pieceType = movePiece.getPieceType().toString();
            }
            notification.setMessage(username + " made move " + pieceType + " " + normalMove.toString());

            connectionManager.broadcast(gameData.gameID(), ctx.session, notification);

            if(chessGame.isGameFinished()) {
                ServerMessage gameOverNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                if(chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                    gameOverNotification.setMessage( gameData.blackUsername() + " (Black) Wins!");
                    connectionManager.broadcast(gameData.gameID(), null, gameOverNotification);
                } else if(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                    gameOverNotification.setMessage(gameData.whiteUsername() + " (White) Wins!");
                    connectionManager.broadcast(gameData.gameID(), null, gameOverNotification);
                } else if(chessGame.isInStalemate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
                    gameOverNotification.setMessage("Nobody wins, very sad :(");
                    connectionManager.broadcast(gameData.gameID(), null, gameOverNotification);
                }

            } else {
                ServerMessage inCheckNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                if(chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
                    inCheckNotification.setMessage(gameData.whiteUsername() + " (White) is in check.");
                    connectionManager.broadcast(gameData.gameID(), null, inCheckNotification);
                } else if(chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
                    inCheckNotification.setMessage(gameData.blackUsername() + " (Black) is in check.");
                    connectionManager.broadcast(gameData.gameID(), null, inCheckNotification);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage(String.format("Error: an error occurred, amigo (in makeMove), %s", e.getMessage()));
            ctx.send(new Gson().toJson(errorMessage));
        }

    }

    private void leave(UserGameCommand userGameCommand, WsMessageContext ctx) {
        try {
            AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
            String username = authData.username();
            int gameID = userGameCommand.getGameID();

            connectionManager.remove(ctx.session);

            GameData gameData = gameDAO.getGame(gameID);
            if(gameData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: you got no game data, guy");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }
            String whiteUsername = gameData.whiteUsername();
            String blackUsername = gameData.blackUsername();
            if(username.equals(whiteUsername)) {
                whiteUsername = null;
            } else if(username.equals(blackUsername)) {
                blackUsername = null;
            }
            GameData updatedGame = new GameData(gameID, whiteUsername, blackUsername, gameData.gameName(), gameData.game());
            gameDAO.updateGame(updatedGame);

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(username + " has left us with a hole in our heart and an unfilled space in our board or spectator area :,) \n You will be missed.");
            connectionManager.broadcast(gameID, ctx.session, notification);
        } catch (DataAccessException e) {
            //intellij says I need this
            throw new RuntimeException(e);
        } catch (IOException e) {
            //intellij says I need this one too
            throw new RuntimeException(e);
        } catch(Exception e) {
            //do something useful with that
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: an error occurred while leaving match");
            ctx.send(new Gson().toJson(errorMessage));
        }
    }

    private void resign(UserGameCommand userGameCommand, WsMessageContext ctx) {
        try {
            AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
            if(authData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: Unauthorized, pal");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }
            GameData gameData = gameDAO.getGame(userGameCommand.getGameID());
            if(gameData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: you ain't got no game data, fred");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }

            String username = authData.username();
            boolean isWhite = false;
            boolean isBlack = false;
            if(username.equals(gameData.whiteUsername())) {
                isWhite = true;
            } else if(username.equals(gameData.blackUsername())) {
                isBlack = true;
            }

            if(!isWhite && !isBlack) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: how would an observer even resign?");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }
            if(gameData.game().isGameFinished()) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                errorMessage.setErrorMessage("Error: the game is already over, bozo");
                ctx.send(new Gson().toJson(errorMessage));
                return;
            }

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage("User: " + username + " has resigned (what a loser)... game over I guess.");
            connectionManager.broadcast(gameData.gameID(), null, notification);

            gameData.game().setResign(true);
            gameDAO.updateGame(gameData);

        } catch (DataAccessException e) {
            //intellij says I need this, per usual
            throw new RuntimeException(e);
        } catch (IOException e) {
            //again, intellij coming in clutch
            throw new RuntimeException(e);
        } catch(Exception e) {
            //do somthing with these errors probably
            ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            errorMessage.setErrorMessage("Error: an error occurred, amigo (in resign)");
            ctx.send(new Gson().toJson(errorMessage));
        }
    }

}
