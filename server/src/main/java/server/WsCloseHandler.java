package server;

import io.javalin.websocket.WsCloseContext;

public interface WsCloseHandler {
    void handleClose(WsCloseContext ctx);
}
