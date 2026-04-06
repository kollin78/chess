package server;

import io.javalin.websocket.WsConnectContext;

public interface WsCloseHandler {
    void handleClose(WsConnectContext ctx);
}
