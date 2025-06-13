package server;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ServerWebSocketHandler {
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ConcurrentHashMap<Session, PlayerInfo>, Integer> organizedSessions = new ConcurrentHashMap<>();
    public static final Gson GSON = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        sessions.put(session, "");
    }

    public static void clearMap(){
        organizedSessions.clear();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        if(message.equals("ping")){
            return;
        }
        if (message.contains("Identification:")) {
            String[] split = message.split(":");
            if (split.length >= 3) {
                int gameID = Integer.parseInt(split[1]);
                String authToken = split[2];
                assignId(session, authToken, gameID);
            } else {
                assignIdForSession(session, split[1]);
            }
            return;
        }
        if (message.contains("AssignGame:")) {
            String[] split = message.split(":");
            assignGame(session, split[1]);
            return;
        }
        UserGameCommand clientMessage = GSON.fromJson(message, UserGameCommand.class);

        if(clientMessage.getCommandType() == UserGameCommand.CommandType.LEAVE){
            handleLeave(session, clientMessage);
        }
        if(clientMessage.getCommandType() == UserGameCommand.CommandType.CONNECT){
            handleConnect(session, clientMessage);
        }
        if(clientMessage.getCommandType() == UserGameCommand.CommandType.RESIGN){
            handleResign(session, clientMessage);
        }
        if(clientMessage.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE){
            handleMakeMove(session, clientMessage);
        }

        //session.getRemote().sendString("template");
    }

    private void handleMakeMove(Session session, UserGameCommand clientMessage){

    }

    public void assignIdForSession(Session senderSession, String ID) {
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : organizedSessions.keySet()) {
            if (sessionMap.containsKey(senderSession)) {
                PlayerInfo info = sessionMap.get(senderSession);
                if (info != null) {
                    info.setAuthToken(ID);
                } else {
                    sessionMap.put(senderSession, new PlayerInfo(ID, "observer", false));
                }
                break;
            }
        }
    }

    private void handleResign(Session session, UserGameCommand clientMessage) throws DataAccessException {
        AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
        if(authData == null){
            sendError(session);
            return;
        }
        if(!sessionExistsInGame(session, clientMessage.getGameID())){
            sendError(session);
            return;
        }
        PlayerInfo info = getPlayerInfo(clientMessage.getAuthToken(), clientMessage.getGameID());
        if(info.hasResigned || info.role.equals("observer")){
            sendError(session);
            return;
        }
        if(hasAnyoneResigned(clientMessage.getGameID())){
            sendError(session);
            return;
        }

        updatePlayerInfo(session, info.getRole(), info.getAuthToken(), true);
        ServerMessage resignMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        String text = "\n" + Server.authDAO.getAuth(clientMessage.getAuthToken()).username() + " has resigned.";
        sendEveryone(session, resignMessage, text, clientMessage.getGameID());
    }

    private void handleConnect(Session session, UserGameCommand clientMessage) throws DataAccessException {
        AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
        if(authData == null){
            sendError(session);
            return;
        }
        if(Server.gameDAO.getGame(clientMessage.getGameID()) == null){
            sendError(session);
            return;
        }
        assignGame(session, clientMessage.getGameID().toString());
        assignId(session, clientMessage.getAuthToken(), clientMessage.getGameID());

        String notif = "";
        if(!getPlayerColor(authData).isEmpty()){
            notif = authData.username() + " has joined the game as " + getPlayerColor(authData) + ".";
            assignRole(session, "player", clientMessage.getAuthToken(), false, clientMessage.getGameID());
        } else {
            notif = authData.username() + " has joined the game as an observer.";
            assignRole(session, "observer", clientMessage.getAuthToken(), false, clientMessage.getGameID());
        }

        ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, sendBack, notif, clientMessage.getGameID());
        //System.out.println(clientMessage.getAuthToken());
        loadGame(session, authData, clientMessage.getGameID());
        //System.out.println(getAuthTokenBySession(session));
        //System.out.println(authData.authToken());
    }

    private void handleLeave(Session session, UserGameCommand clientMessage) throws DataAccessException {
        AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
        if(authData == null){
            sendError(session);
            return;
        }
        if(!sessionExistsInGame(session, clientMessage.getGameID())){
            sendError(session);
            return;
        }
        GameData game = Server.gameDAO.getGame(clientMessage.getGameID());
        if((game.whiteUsername() != null && !game.whiteUsername().equals(authData.username())) && (game.blackUsername() != null && !game.blackUsername().equals(authData.username()))){
            return;
        }

        String notif = authData.username() + " has left the game.";
        GameData newGameInfo;
        if(game.whiteUsername() != null && game.whiteUsername().equals(authData.username())){
            newGameInfo = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
        } else {
            newGameInfo = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
        }
        Server.gameDAO.replaceGameData(game, newGameInfo);
        ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, sendBack, notif, clientMessage.getGameID());
        System.out.println(clientMessage.getAuthToken());
        removePlayerFromGame(session, clientMessage.getGameID());
    }

    public boolean sessionExistsInGame(Session sessionToCheck, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                return entry.getKey().containsKey(sessionToCheck);
            }
        }
        return false;
    }

    private void sendError(Session session){
        ServerMessage serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        serverMessage.errorMessage = "Error.";
        if (session.isOpen()) {
            try {
                session.getRemote().sendString(GSON.toJson(serverMessage, ServerMessage.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void loadGame(Session senderSession, AuthData auth, int gameId) throws DataAccessException {
        ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        GameData game = Server.gameDAO.getGame(gameId);
        sendBack.game = game;
        sendBack.auth = auth;
        sendBack.message = null;

        if (senderSession.isOpen()) {
            try {
                senderSession.getRemote().sendString(GSON.toJson(sendBack, ServerMessage.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendEveryoneElse(Session senderSession, ServerMessage message, String text, int gameID) {
        message.game = null;
        message.auth = null;
        message.message = (text != null) ? text : null;

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                for (Session session : entry.getKey().keySet()) {
                    if (session.isOpen() && !session.equals(senderSession)) {
                        try {
                            AuthData data = Server.authDAO.getAuth(getAuthTokenBySession(session));
                            System.out.println("Sent \'" + message.message + "\' to: " + data.username());
                            session.getRemote().sendString(GSON.toJson(message, ServerMessage.class));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DataAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
            }
        }
    }

    public void sendEveryone(Session senderSession, ServerMessage message, String text, int gameID) {
        message.game = null;
        message.auth = null;
        message.message = (text != null) ? text : null;

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                for (Session session : entry.getKey().keySet()) {
                    if (session.isOpen()) {
                        try {
                            AuthData data = Server.authDAO.getAuth(getAuthTokenBySession(session));
                            //System.out.println("Sent \'" + message.message + "\' to: " + data.username());
                            session.getRemote().sendString(GSON.toJson(message, ServerMessage.class));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DataAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                break;
            }
        }
    }


    public void assignId(Session senderSession, String ID, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().containsKey(senderSession)) {
                PlayerInfo info = entry.getKey().get(senderSession);
                if (info != null) {
                    info.setAuthToken(ID);
                } else {
                    entry.getKey().put(senderSession, new PlayerInfo(ID, "observer", false));
                }
                break;
            }
        }
    }


    public void assignGame(Session senderSession, String gameID) {
        Integer targetGameId = Integer.valueOf(gameID);

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue().equals(targetGameId)) {
                entry.getKey().put(senderSession, new PlayerInfo("", "observer", false));
                return;
            }
        }

        ConcurrentHashMap<Session, PlayerInfo> newSessionMap = new ConcurrentHashMap<>();
        newSessionMap.put(senderSession, new PlayerInfo("", "observer", false));
        organizedSessions.put(newSessionMap, targetGameId);
    }

    public void assignRole(Session senderSession, String role, String token, boolean hasResigned, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().containsKey(senderSession)) {
                PlayerInfo info = entry.getKey().get(senderSession);
                if (info != null) {
                    info.setRole(role);
                    info.setAuthToken(token);
                    info.setResigned(hasResigned);
                } else {
                    entry.getKey().put(senderSession, new PlayerInfo(token, role, hasResigned));
                }
                break;
            }
        }
    }

    public String getRole(String authToken, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                for (PlayerInfo info : sessionMap.values()) {
                    if (info != null && authToken.equals(info.getAuthToken())) {
                        return info.getRole();
                    }
                }
            }
        }
        return null;
    }

    public PlayerInfo getPlayerInfo(String authToken, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                for (PlayerInfo info : sessionMap.values()) {
                    if (info != null && authToken.equals(info.getAuthToken())) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
    public String getAuthTokenBySession(Session sessionToCheck) {
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : organizedSessions.keySet()) {
            if (sessionMap.containsKey(sessionToCheck)) {
                PlayerInfo info = sessionMap.get(sessionToCheck);
                if (info != null) {
                    return info.getAuthToken();
                }
            }
        }
        return null;
    }

    public void updatePlayerInfo(Session session, String role, String token, boolean hasResigned) {
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : organizedSessions.keySet()) {
            if (sessionMap.containsKey(session)) {
                PlayerInfo info = sessionMap.get(session);
                if (info != null) {
                    info.setRole(role);
                    info.setAuthToken(token);
                    info.setResigned(hasResigned);
                }
                break;
            }
        }
    }
    public boolean hasAnyoneResigned(int gameID) {
        List<PlayerInfo> playersInGame = new ArrayList<>();

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                for (PlayerInfo info : sessionMap.values()) {
                    if (info != null) {
                        if(info.hasResigned == true){return true;}
                    }
                }
                break;
            }
        }

        return false;
    }
    public void removePlayerFromGame(Session sessionToRemove, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : organizedSessions.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                sessionMap.remove(sessionToRemove);
                break;
            }
        }
    }
}