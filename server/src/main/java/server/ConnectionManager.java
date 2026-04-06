package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public ConcurrentHashMap<Integer, HashSet<Session>> games = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Session, String> users = new ConcurrentHashMap<>();

    public void add(int gameID, String username, Session session) {
        games.computeIfAbsent(gameID, k -> new HashSet<>());
        games.get(gameID).add(session);
        users.put(session, username);
    }

    public void remove(Session session) {
        users.remove(session);

        for(Integer gameID : games.keySet()) {
            HashSet<Session> activeSessions = games.get(gameID);
            if(activeSessions != null) {
                activeSessions.remove(session);
            }
        }
    }

    public void broadcast(int gameID, Session moversSession, ServerMessage message) throws IOException {
        HashSet<Session> activeSessions = games.get(gameID);

        if(activeSessions == null) {
            return;
        }
        String jsonMsg = new Gson().toJson(message);

        for(Session session : activeSessions) {
            if(session.isOpen()) {
                if(!session.equals(moversSession)) {
                    session.getRemote().sendString(jsonMsg);
                }
            }
        }
    }
}
