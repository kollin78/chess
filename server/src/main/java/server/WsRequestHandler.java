package server;

import com.google.gson.Gson;
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
                case MAKE_MOVE -> makeMove();
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

    private void makeMove() {

    }

    private void leave() {

    }

    private void resign() {

    }

}
