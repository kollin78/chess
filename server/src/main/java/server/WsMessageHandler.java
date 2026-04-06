package server;


import io.javalin.websocket.WsMessageContext;

public interface WsMessageHandler {
    void handleMessage(WsMessageContext ctx);
}
