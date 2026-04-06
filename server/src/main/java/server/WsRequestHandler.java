package server;

import com.google.gson.Gson;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.server.session.*;
import websocket.commands.UserGameCommand;


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
    public void handleMessage(WsMessageContext ctx) {
        try {
            String jsonMsg = ctx.message();
            UserGameCommand userGameCommand = new Gson().fromJson(jsonMsg, UserGameCommand.class);

            switch(userGameCommand.getCommandType()) {
                case CONNECT -> doConnect();
                case MAKE_MOVE -> makeMove();
                case LEAVE -> leave();
                case RESIGN -> resign();
                default -> System.out.println("Please enter a valid command");
            }
        } catch(Exception e) {
            //do smth with error
        }
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
