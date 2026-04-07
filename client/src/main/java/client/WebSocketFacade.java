package client;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.*;
import websocket.messages.ServerMessage;

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
}
