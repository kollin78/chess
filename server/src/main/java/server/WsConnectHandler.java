package server;

import io.javalin.websocket.WsConnectContext;

public interface WsConnectHandler {
    void handleConnect(WsConnectContext ctx);
}
