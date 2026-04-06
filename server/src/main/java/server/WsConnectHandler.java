package server;

import jakarta.websocket.Session;

public interface WsConnectHandler {
    void onConnect(String authToken, Session session);
}
