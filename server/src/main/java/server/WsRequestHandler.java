package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.server.session.*;
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
    public void handleClose(WsConnectContext ctx) {
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
                case LEAVE -> leave();
                case RESIGN -> resign();
                default -> System.out.println("Please enter a valid command");
            }
        } catch(Exception e) {
            //do smth with error
        }
    }

    private void doConnect(UserGameCommand userGameCommand, WsMessageContext ctx) {
        try {
            AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
            if(authData == null) {
                //error message
                return;
            }
            GameData gameData = gameDAO.getGame(userGameCommand.getGameID());
            if(gameData == null) {
                //error message
                return;
            }

            String username = authData.username();
            int gameID = userGameCommand.getGameID();
            connectionManager.add(gameID, username, ctx.session);

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(gameData);
            ctx.send(new Gson().toJson(loadGame));

            String message = (username + " has joined the game, hold on to your booty.");
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(message);
            connectionManager.broadcast(gameID, ctx.session, notification);
        } catch(DataAccessException e) {
            //do smth with error
        } catch (IOException e) {
            //intellij said we needed this
            throw new RuntimeException(e);
        }
    }

    private void makeMove(String jsonMsg, WsMessageContext ctx) throws Exception {
        UserGameCommand userGameCommand = new Gson().fromJson(jsonMsg, UserGameCommand.class);
        AuthData authData = authDAO.getAuth(userGameCommand.getAuthToken());
        if(authData == null) {
            //let user know they're dumb
            return;
        }
        GameData gameData = gameDAO.getGame(userGameCommand.getGameID());
        if(gameData == null) {
            //let user know they're dumb in a different way
            return;
        }
        ChessGame chessGame = gameData.game();
        if(chessGame.isGameFinished()) {
            //let user know they are dumb in a whole new way
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
            //tell player to way their turn
            return;
        }

        try {
            JsonObject jsonMove = new Gson().fromJson(jsonMsg, JsonObject.class);
            ChessMove normalMove = new Gson().fromJson(jsonMove.get("move"), ChessMove.class);
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
            notification.setMessage(username + " made move " + normalMove.toString());
            connectionManager.broadcast(gameData.gameID(), ctx.session, notification);

            if(chessGame.isGameFinished()) {
                if(chessGame.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                    notification.setMessage("Black Wins!");
                } else if(chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                    notification.setMessage("White Wins!");
                } else if(chessGame.isInStalemate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
                    notification.setMessage("Nobody wins, very sad :(");
                } else if(chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
                    notification.setMessage("White is in check.");
                } else if(chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
                    notification.setMessage("Black is in check.");
                }
                connectionManager.broadcast(gameData.gameID(), null, notification);
            }
        } catch(Exception e) {
            //tell user smth brokey
        }

    }

    private void leave() {

    }

    private void resign() {

    }

}
