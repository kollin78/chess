package server;


import com.google.gson.Gson;
import io.javalin.websocket.WsConnectContext;
import jakarta.websocket.Session;
import org.eclipse.jetty.server.session.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import jakarta.websocket.Session;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler{

    private final ConnectionManager connectionManager = new ConnectionManager();

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
    public void handleMessage(WsConnectContext ctx) {

    }

    private void doConnect() {

    }

    private void makeMove() {

    }

    private void leave() {

    }

    private void resign() {

    }

}
