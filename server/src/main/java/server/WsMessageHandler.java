package server;


import websocket.commands.UserGameCommand;

public interface WsMessageHandler {
    void onMessage(String authToken, UserGameCommand userGameCommand);
}
