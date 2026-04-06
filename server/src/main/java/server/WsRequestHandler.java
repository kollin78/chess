package server;


import com.google.gson.Gson;
import jakarta.websocket.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import jakarta.websocket.Session;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler{

    @Override
    public void onClose(String authToken) {
        System.out.println("Disconnecting");
    }

    @Override
    public void onConnect(String authToken, Session session) {
        System.out.println("Now Connected: " + session.getId());
    }

    @Override
    public void onMessage(String authToken, UserGameCommand userGameCommand) {

    }
}
