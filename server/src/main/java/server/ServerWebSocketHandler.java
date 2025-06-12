package server;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ServerWebSocketHandler {
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ConcurrentHashMap<Session, String>, Integer> organizedSessions = new ConcurrentHashMap<>();
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
        if (message.contains("Identification:")) {
            String[] split = message.split(":");
            assignId(session, split[1]);
            return;
        }
        if (message.contains("AssignGame:")) {
            String[] split = message.split(":");
            assignGame(session, split[1]);
            return;
        }
        UserGameCommand clientMessage = GSON.fromJson(message, UserGameCommand.class);

        if(clientMessage.getCommandType() == UserGameCommand.CommandType.LEAVE){
            AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
            String notif = authData.username() + " has left the game.";
            ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notif);
            sendEveryoneElse(session, GSON.toJson(sendBack, ServerMessage.class), clientMessage.getGameID());
        }
        if(clientMessage.getCommandType() == UserGameCommand.CommandType.CONNECT){
            AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
            String notif = "";
            if(!clientMessage.message.contains("observer")){
                notif = authData.username() + " has joined the game as " + getPlayerColor(authData) + ".";
            } else {
                notif = authData.username() + " has joined the game as an observer.";
            }

            ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notif);
            sendEveryoneElse(session, GSON.toJson(sendBack, ServerMessage.class), clientMessage.getGameID());
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

    public String getPlayerColor(AuthData token) throws DataAccessException {
        Collection<GameData> games = Server.gameDAO.getGames();
        Integer gameID = -1;
        for(GameData game : games){
            if(game.blackUsername() != null && game.blackUsername().equals(token.username())){
                return "black";
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(token.username())){
                return "white";
            }
        }
        return "";
    }

    public void sendEveryoneElse(Session senderSession, String message, int gameID){
        for (Map.Entry<ConcurrentHashMap<Session, String>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, String> sessionMap = entry.getKey();

                for (Session session : sessionMap.keySet()) {
                    if (session.isOpen() && !session.equals(senderSession)) {
                        try {
                            session.getRemote().sendString(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                break;
            }
        }
    }

    public void assignId(Session senderSession, String ID) {
        for (ConcurrentHashMap<Session, String> sessionMap : organizedSessions.keySet()) {
            if (sessionMap.containsKey(senderSession)) {
                sessionMap.put(senderSession, ID);
                break;
            }
        }
    }

    public void assignGame(Session senderSession, String gameID) {
        Integer targetGameId = Integer.valueOf(gameID);

        for (Map.Entry<ConcurrentHashMap<Session, String>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue().equals(targetGameId)) {
                entry.getKey().put(senderSession, "");
                return;
            }
        }

        ConcurrentHashMap<Session, String> newSessionMap = new ConcurrentHashMap<>();
        newSessionMap.put(senderSession, "");
        organizedSessions.put(newSessionMap, targetGameId);
    }
}
