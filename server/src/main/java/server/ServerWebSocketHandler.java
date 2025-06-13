package server;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
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
    private static final ConcurrentHashMap<Session, String> SESSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ConcurrentHashMap<Session, PlayerInfo>, Integer> ORGANIZED_SESSIONS =
            new ConcurrentHashMap<>();
    public static final Gson GSON = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        SESSIONS.put(session, "");
    }

    public static void clearMap() {
        ORGANIZED_SESSIONS.clear();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException, InvalidMoveException {
        if (message.equals("ping")) {
            return;
        }

        if (handleSpecialMessages(session, message)) {
            return;
        }

        UserGameCommand clientMessage = GSON.fromJson(message, UserGameCommand.class);
        handleCommand(session, clientMessage);
    }

    private boolean handleSpecialMessages(Session session, String message) {
        if (message.contains("Identification:")) {
            handleIdentification(session, message);
            return true;
        }

        if (message.contains("AssignGame:")) {
            String[] split = message.split(":");
            assignGame(session, split[1]);
            return true;
        }

        return false;
    }

    private void handleIdentification(Session session, String message) {
        String[] split = message.split(":");
        if (split.length >= 3) {
            int gameID = Integer.parseInt(split[1]);
            String authToken = split[2];
            assignId(session, authToken, gameID);
        } else {
            assignIdForSession(session, split[1]);
        }
    }

    private void handleCommand(Session session, UserGameCommand command) throws DataAccessException, InvalidMoveException {
        switch (command.getCommandType()) {
            case LEAVE -> handleLeave(session, command);
            case CONNECT -> handleConnect(session, command);
            case RESIGN -> handleResign(session, command);
            case MAKE_MOVE -> handleMakeMove(session, command);
        }
    }

    private void handleMakeMove(Session session, UserGameCommand clientMessage) throws DataAccessException, InvalidMoveException {
        GameData game = Server.gameDAO.getGame(clientMessage.getGameID());
        GameData oldGame = Server.gameDAO.getGame(clientMessage.getGameID());
        AuthData dt = Server.authDAO.getAuth(clientMessage.getAuthToken());

        if (dt == null) {
            sendError(session);
            return;
        }

        if (getRole(clientMessage.getAuthToken(), clientMessage.getGameID()).equals("observer")) {
            sendError(session);
            return;
        }

        String currentPlayerUsername = game.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE) ?
                game.whiteUsername() : game.blackUsername();
        if (!currentPlayerUsername.equals(Server.authDAO.getAuth(clientMessage.getAuthToken()).username())) {
            sendError(session);
            return;
        }

        if (hasAnyoneResigned(clientMessage.getGameID())) {
            sendError(session);
            return;
        }

        if (game.game().whiteInCheckmate || game.game().blackInCheckmate) {
            sendError(session);
            return;
        }

        try {
            game.game().makeMove(clientMessage.move);
            Server.gameDAO.replaceGameData(oldGame, game);
        } catch (Exception e) {
            sendError(session);
            return;
        }

        List<Session> gameSessions = getSessionsByGameId(clientMessage.getGameID());
        ServerMessage newMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        ChessPosition start = clientMessage.move.getStartPosition();
        ChessPosition end = clientMessage.move.getEndPosition();
        newMessage.message = "\n" + dt.username() + " moved a piece from " + start.x + " " + getLetter(start.y) +
                " to " + end.x + " " + getLetter(end.y);
        sendEveryoneElse(session, newMessage, newMessage.message, clientMessage.getGameID());

        ServerMessage newerMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        loadGames(gameSessions, dt, clientMessage.getGameID());

        if (game.game().blackInCheckmate) {
            newerMessage.errorMessage = "\n" + game.blackUsername() + " is in checkmate!";
        }
        if (game.game().whiteInCheckmate) {
            newerMessage.errorMessage = "\n" + game.whiteUsername() + " is in checkmate!";
        }

        if (newerMessage.errorMessage != null) {
            sendEveryoneElse(session, newerMessage, newerMessage.errorMessage, clientMessage.getGameID());
        }
    }

    private void handleResign(Session session, UserGameCommand clientMessage) throws DataAccessException {
        AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
        if (authData == null) {
            sendError(session);
            return;
        }

        if (!sessionExistsInGame(session, clientMessage.getGameID())) {
            sendError(session);
            return;
        }

        PlayerInfo info = getPlayerInfo(clientMessage.getAuthToken(), clientMessage.getGameID());
        if (info.hasResigned || info.role.equals("observer")) {
            sendError(session);
            return;
        }

        if (hasAnyoneResigned(clientMessage.getGameID())) {
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
        if (authData == null) {
            sendError(session);
            return;
        }

        if (Server.gameDAO.getGame(clientMessage.getGameID()) == null) {
            sendError(session);
            return;
        }

        assignGame(session, clientMessage.getGameID().toString());
        assignId(session, clientMessage.getAuthToken(), clientMessage.getGameID());

        String notif = "";
        if (!getPlayerColor(authData).isEmpty()) {
            notif = authData.username() + " has joined the game as " + getPlayerColor(authData) + ".";
            assignRole(session, "player", clientMessage.getAuthToken(), false, clientMessage.getGameID());
        } else {
            notif = authData.username() + " has joined the game as an observer.";
            assignRole(session, "observer", clientMessage.getAuthToken(), false, clientMessage.getGameID());
        }

        ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, sendBack, notif, clientMessage.getGameID());
        loadGame(session, authData, clientMessage.getGameID());
    }

    private void handleLeave(Session session, UserGameCommand clientMessage) throws DataAccessException {
        AuthData authData = Server.authDAO.getAuth(clientMessage.getAuthToken());
        if (authData == null) {
            sendError(session);
            return;
        }

        if (!sessionExistsInGame(session, clientMessage.getGameID())) {
            sendError(session);
            return;
        }

        GameData game = Server.gameDAO.getGame(clientMessage.getGameID());
        if ((game.whiteUsername() != null && !game.whiteUsername().equals(authData.username())) &&
                (game.blackUsername() != null && !game.blackUsername().equals(authData.username()))) {
            return;
        }

        String notif = authData.username() + " has left the game.";
        GameData newGameInfo;
        if (game.whiteUsername() != null && game.whiteUsername().equals(authData.username())) {
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

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        SESSIONS.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void sendError(Session session) {
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

    public void sendEveryoneElse(Session senderSession, ServerMessage message, String text, int gameID) {
        message.game = null;
        message.auth = null;
        message.message = (text != null) ? text : null;

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
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

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID) {
                for (Session session : entry.getKey().keySet()) {
                    if (session.isOpen()) {
                        try {
                            session.getRemote().sendString(GSON.toJson(message, ServerMessage.class));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
        }
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

    public void loadGames(List<Session> senderSessions, AuthData auth, int gameId) throws DataAccessException {
        ServerMessage sendBack = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        GameData game = Server.gameDAO.getGame(gameId);
        sendBack.game = game;
        sendBack.auth = auth;
        sendBack.message = null;

        for (Session senderSession : senderSessions) {
            if (senderSession.isOpen()) {
                try {
                    senderSession.getRemote().sendString(GSON.toJson(sendBack, ServerMessage.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void assignGame(Session senderSession, String gameID) {
        Integer targetGameId = Integer.valueOf(gameID);

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue().equals(targetGameId)) {
                entry.getKey().put(senderSession, new PlayerInfo("", "observer", false));
                return;
            }
        }

        ConcurrentHashMap<Session, PlayerInfo> newSessionMap = new ConcurrentHashMap<>();
        newSessionMap.put(senderSession, new PlayerInfo("", "observer", false));
        ORGANIZED_SESSIONS.put(newSessionMap, targetGameId);
    }

    public void assignId(Session senderSession, String id, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().containsKey(senderSession)) {
                PlayerInfo info = entry.getKey().get(senderSession);
                if (info != null) {
                    info.setAuthToken(id);
                } else {
                    entry.getKey().put(senderSession, new PlayerInfo(id, "observer", false));
                }
                break;
            }
        }
    }

    public void assignIdForSession(Session senderSession, String id) {
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : ORGANIZED_SESSIONS.keySet()) {
            if (sessionMap.containsKey(senderSession)) {
                PlayerInfo info = sessionMap.get(senderSession);
                if (info != null) {
                    info.setAuthToken(id);
                } else {
                    sessionMap.put(senderSession, new PlayerInfo(id, "observer", false));
                }
                break;
            }
        }
    }

    public void assignRole(Session senderSession, String role, String token, boolean hasResigned, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
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
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
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
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
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
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : ORGANIZED_SESSIONS.keySet()) {
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
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : ORGANIZED_SESSIONS.keySet()) {
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
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                for (PlayerInfo info : sessionMap.values()) {
                    if (info != null) {
                        if (info.hasResigned == true) {
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    public boolean sessionExistsInGame(Session sessionToCheck, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID) {
                return entry.getKey().containsKey(sessionToCheck);
            }
        }
        return false;
    }

    public void removePlayerFromGame(Session sessionToRemove, int gameID) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                sessionMap.remove(sessionToRemove);
                break;
            }
        }
    }

    public List<Session> getSessionsByGameId(int gameId) {
        List<Session> sessions = new ArrayList<>();
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameId) {
                sessions.addAll(entry.getKey().keySet());
                break;
            }
        }
        return sessions;
    }

    public String getPlayerColor(AuthData token) throws DataAccessException {
        Collection<GameData> games = Server.gameDAO.getGames();
        for (GameData game : games) {
            if (game.blackUsername() != null && game.blackUsername().equals(token.username())) {
                return "black";
            }
            if (game.whiteUsername() != null && game.whiteUsername().equals(token.username())) {
                return "white";
            }
        }
        return "";
    }

    public static String getLetter(int num) {
        String[] letters = {"", "a", "b", "c", "d", "e", "f", "g", "h"};
        return (num >= 1 && num <= 8) ? letters[num] : "";
    }
}