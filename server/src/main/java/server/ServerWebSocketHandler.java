package server;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ServerWebSocketHandler {
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();
    public static final Gson GSON = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        sessions.put(session, "");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        if(message.equals("ping")){
            return;
        }
        UserGameCommand clientMessage = GSON.fromJson(message, UserGameCommand.class);

        if(clientMessage.getCommandType() == UserGameCommand.CommandType.LEAVE){
            AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
            System.out.println(authData.username() + " has left the game.");
        }

        //session.getRemote().sendString("template");
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
