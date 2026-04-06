package server;

public interface WsCloseHandler {
    void onClose(String authToken);
}
