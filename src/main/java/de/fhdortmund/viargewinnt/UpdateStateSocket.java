package de.fhdortmund.viargewinnt;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class UpdateStateSocket {
    private static final Map<Long, List<Session>> gameSessions = new HashMap<>();
    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();

    static void sendUpdate(long gameId, TurnEvent event) {
        List<Session> sessions = gameSessions.get(gameId);
        if (sessions == null) {
            return;
        }

        sessions.forEach(session -> {
            try {
                session.getRemote().sendString(gson.toJson(event));
            } catch (IOException e) {
                System.out.println("could not inform a user " + e.getMessage());
            }
        });
    }

    @OnWebSocketConnect
    public void connected(Session session) {

    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        gameSessions.values().forEach(sessions -> sessions.remove(session));
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        long gameId = Integer.parseInt(message);
        System.out.println("Got: " + gameId);

        List<Session> sessions = gameSessions.computeIfAbsent(gameId, id -> new ArrayList<>());
        sessions.add(session);
    }
}
