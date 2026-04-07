package client;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private ChessClient client;

    public WebSocketFacade(String serverURL, ChessClient client) throws Exception {
        this.client = client;
        serverURL = serverURL.replace("http", "ws");
        URI socketURI = new URI(serverURL + "/ws");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = (Session) container.connectToServer(this, socketURI);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                ServerMessage serverMessage = new Gson().fromJson(s, ServerMessage.class);
                client.messageHandler(serverMessage);
            }
        });
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws IOException {
        var cmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void makeMove(String authToken, int gameID, ChessMove chessMove) throws IOException {
        var cmd = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
        cmd.setMove(chessMove);
        this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void leave(String authToken, int gameID) throws IOException {
        var cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }

    public void resign(String authToken, int gameID) throws IOException {
        var cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(cmd));
    }
}
