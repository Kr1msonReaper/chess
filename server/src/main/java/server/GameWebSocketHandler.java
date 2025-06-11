package server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class GameWebSocketHandler {
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        sessions.put(session, "");
        session.getRemote().sendString("Connected to WebSocket!");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        session.getRemote().sendString("template");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
