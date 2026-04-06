package server;


import io.javalin.websocket.WsConnectContext;

public interface WsMessageHandler {
    void handleMessage(WsConnectContext ctx);
}
