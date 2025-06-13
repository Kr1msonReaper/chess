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
        if (message.equals("ping") || handleSpecialMessages(session, message)) {
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
            assignId(session, split[2], Integer.parseInt(split[1]));
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

    private void handleMakeMove(Session session, UserGameCommand cmd) throws DataAccessException, InvalidMoveException {
        GameData game = Server.gameDAO.getGame(cmd.getGameID());
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());

        if (!validateMoveRequest(auth, cmd, game)) {
            sendError(session);
            return;
        }

        if (!executeMoveAndUpdate(game, cmd)) {
            sendError(session);
            return;
        }

        notifyMoveCompletion(session, cmd, auth, game);
    }

    private boolean validateMoveRequest(AuthData auth, UserGameCommand cmd, GameData game) {
        if (auth == null || getRole(cmd.getAuthToken(), cmd.getGameID()).equals("observer")) {
            return false;
        }

        String currentPlayer = game.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE) ?
                game.whiteUsername() : game.blackUsername();

        return currentPlayer.equals(auth.username()) &&
                !hasAnyoneResigned(cmd.getGameID()) &&
                !game.game().whiteInCheckmate &&
                !game.game().blackInCheckmate;
    }

    private boolean executeMoveAndUpdate(GameData game, UserGameCommand cmd) throws DataAccessException {
        try {
            GameData oldGame = Server.gameDAO.getGame(cmd.getGameID());
            game.game().makeMove(cmd.move);
            Server.gameDAO.replaceGameData(oldGame, game);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void notifyMoveCompletion(Session session, UserGameCommand cmd, AuthData auth, GameData game)
            throws DataAccessException {
        ChessPosition start = cmd.move.getStartPosition();
        ChessPosition end = cmd.move.getEndPosition();
        String moveText = "\n" + auth.username() + " moved a piece from " + start.x + " " +
                getLetter(start.y) + " to " + end.x + " " + getLetter(end.y);

        ServerMessage moveMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, moveMsg, moveText, cmd.getGameID());

        List<Session> sessions = getSessionsByGameId(cmd.getGameID());
        loadGames(sessions, auth, cmd.getGameID());

        notifyCheckmate(session, game, cmd.getGameID());
    }

    private void notifyCheckmate(Session session, GameData game, int gameID) {
        String checkmateMsg = null;
        if (game.game().blackInCheckmate) {
            checkmateMsg = "\n" + game.blackUsername() + " is in checkmate!";
        }
        if (game.game().whiteInCheckmate) {
            checkmateMsg = "\n" + game.whiteUsername() + " is in checkmate!";
        }

        if (checkmateMsg != null) {
            ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            sendEveryoneElse(session, msg, checkmateMsg, gameID);
        }
    }

    private void handleResign(Session session, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());
        if (!validateResignRequest(session, cmd, auth)) {
            sendError(session);
            return;
        }

        PlayerInfo info = getPlayerInfo(cmd.getAuthToken(), cmd.getGameID());
        updatePlayerInfo(session, info.getRole(), info.getAuthToken(), true);

        String text = "\n" + auth.username() + " has resigned.";
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryone(session, msg, text, cmd.getGameID());
    }

    private boolean validateResignRequest(Session session, UserGameCommand cmd, AuthData auth) {
        if (auth == null || !sessionExistsInGame(session, cmd.getGameID())) {
            return false;
        }

        PlayerInfo info = getPlayerInfo(cmd.getAuthToken(), cmd.getGameID());
        return info != null && !info.hasResigned && !info.role.equals("observer") &&
                !hasAnyoneResigned(cmd.getGameID());
    }

    private void handleConnect(Session session, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());
        if (auth == null || Server.gameDAO.getGame(cmd.getGameID()) == null) {
            sendError(session);
            return;
        }

        setupPlayerConnection(session, cmd, auth);
        loadGame(session, auth, cmd.getGameID());
    }

    private void setupPlayerConnection(Session session, UserGameCommand cmd, AuthData auth)
            throws DataAccessException {
        assignGame(session, cmd.getGameID().toString());
        assignId(session, cmd.getAuthToken(), cmd.getGameID());

        String color = getPlayerColor(auth);
        String notif;
        String role;

        if (!color.isEmpty()) {
            notif = auth.username() + " has joined the game as " + color + ".";
            role = "player";
        } else {
            notif = auth.username() + " has joined the game as an observer.";
            role = "observer";
        }

        assignRole(session, role, cmd.getAuthToken(), false, cmd.getGameID());
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, msg, notif, cmd.getGameID());
    }

    private void handleLeave(Session session, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());
        if (auth == null || !sessionExistsInGame(session, cmd.getGameID())) {
            sendError(session);
            return;
        }

        GameData game = Server.gameDAO.getGame(cmd.getGameID());
        if ((game.whiteUsername() != null && !game.whiteUsername().equals(auth.username())) &&
                (game.blackUsername() != null && !game.blackUsername().equals(auth.username()))) {
            return;
        }

        updateGameAfterLeave(game, auth.username());
        notifyPlayerLeft(session, auth.username(), cmd.getGameID());
        removePlayerFromGame(session, cmd.getGameID());
    }

    private void updateGameAfterLeave(GameData game, String username) throws DataAccessException {
        GameData newGame;
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            newGame = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
        } else {
            newGame = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
        }
        Server.gameDAO.replaceGameData(game, newGame);
    }

    private void notifyPlayerLeft(Session session, String username, int gameID) {
        String notif = username + " has left the game.";
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, msg, notif, gameID);
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
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        msg.errorMessage = "Error.";
        sendToSession(session, msg);
    }

    private void sendToSession(Session session, ServerMessage message) {
        if (session.isOpen()) {
            try {
                session.getRemote().sendString(GSON.toJson(message, ServerMessage.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendEveryoneElse(Session sender, ServerMessage message, String text, int gameID) {
        message.game = null;
        message.auth = null;
        message.message = text;

        sendToGameSessions(gameID, message, sender, false);
    }

    public void sendEveryone(Session sender, ServerMessage message, String text, int gameID) {
        message.game = null;
        message.auth = null;
        message.message = text;

        sendToGameSessions(gameID, message, sender, true);
    }

    private void sendToGameSessions(int gameID, ServerMessage message, Session sender, boolean includeSender) {
        getGameSessionMap(gameID).ifPresent(sessionMap -> {
            for (Session session : sessionMap.keySet()) {
                if (session.isOpen() && (includeSender || !session.equals(sender))) {
                    sendToSession(session, message);
                }
            }
        });
    }

    private java.util.Optional<ConcurrentHashMap<Session, PlayerInfo>> getGameSessionMap(int gameID) {
        return ORGANIZED_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue() == gameID)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public void loadGame(Session session, AuthData auth, int gameId) throws DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.game = Server.gameDAO.getGame(gameId);
        msg.auth = auth;
        msg.message = null;
        sendToSession(session, msg);
    }

    public void loadGames(List<Session> sessions, AuthData auth, int gameId) throws DataAccessException {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.game = Server.gameDAO.getGame(gameId);
        msg.auth = auth;
        msg.message = null;

        sessions.forEach(session -> sendToSession(session, msg));
    }

    public void assignGame(Session session, String gameID) {
        Integer targetGameId = Integer.valueOf(gameID);

        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue().equals(targetGameId)) {
                entry.getKey().put(session, new PlayerInfo("", "observer", false));
                return;
            }
        }

        ConcurrentHashMap<Session, PlayerInfo> newSessionMap = new ConcurrentHashMap<>();
        newSessionMap.put(session, new PlayerInfo("", "observer", false));
        ORGANIZED_SESSIONS.put(newSessionMap, targetGameId);
    }

    public void assignId(Session session, String id, int gameID) {
        updateSessionInGame(gameID, session, info -> info.setAuthToken(id),
                () -> new PlayerInfo(id, "observer", false));
    }

    public void assignIdForSession(Session session, String id) {
        for (ConcurrentHashMap<Session, PlayerInfo> sessionMap : ORGANIZED_SESSIONS.keySet()) {
            if (sessionMap.containsKey(session)) {
                updatePlayerInfoInMap(sessionMap, session, info -> info.setAuthToken(id),
                        () -> new PlayerInfo(id, "observer", false));
                break;
            }
        }
    }

    public void assignRole(Session session, String role, String token, boolean hasResigned, int gameID) {
        updateSessionInGame(gameID, session, info -> {
            info.setRole(role);
            info.setAuthToken(token);
            info.setResigned(hasResigned);
        }, () -> new PlayerInfo(token, role, hasResigned));
    }

    private void updateSessionInGame(int gameID, Session session,
                                     java.util.function.Consumer<PlayerInfo> updater,
                                     java.util.function.Supplier<PlayerInfo> creator) {
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().containsKey(session)) {
                updatePlayerInfoInMap(entry.getKey(), session, updater, creator);
                break;
            }
        }
    }

    private void updatePlayerInfoInMap(ConcurrentHashMap<Session, PlayerInfo> map, Session session,
                                       java.util.function.Consumer<PlayerInfo> updater,
                                       java.util.function.Supplier<PlayerInfo> creator) {
        PlayerInfo info = map.get(session);
        if (info != null) {
            updater.accept(info);
        } else {
            map.put(session, creator.get());
        }
    }

    public String getRole(String authToken, int gameID) {
        return findPlayerInfoInGame(gameID, authToken)
                .map(PlayerInfo::getRole)
                .orElse(null);
    }

    public PlayerInfo getPlayerInfo(String authToken, int gameID) {
        return findPlayerInfoInGame(gameID, authToken).orElse(null);
    }

    private java.util.Optional<PlayerInfo> findPlayerInfoInGame(int gameID, String authToken) {
        return ORGANIZED_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue() == gameID)
                .flatMap(entry -> entry.getKey().values().stream())
                .filter(info -> info != null && authToken.equals(info.getAuthToken()))
                .findFirst();
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
        return ORGANIZED_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue() == gameID)
                .flatMap(entry -> entry.getKey().values().stream())
                .filter(info -> info != null)
                .anyMatch(info -> info.hasResigned);
    }

    public boolean sessionExistsInGame(Session session, int gameID) {
        return ORGANIZED_SESSIONS.entrySet().stream()
                .anyMatch(entry -> entry.getValue() == gameID && entry.getKey().containsKey(session));
    }

    public void removePlayerFromGame(Session session, int gameID) {
        ORGANIZED_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue() == gameID)
                .findFirst()
                .ifPresent(entry -> entry.getKey().remove(session));
    }

    public List<Session> getSessionsByGameId(int gameId) {
        return ORGANIZED_SESSIONS.entrySet().stream()
                .filter(entry -> entry.getValue() == gameId)
                .findFirst()
                .map(entry -> new ArrayList<>(entry.getKey().keySet()))
                .orElse(new ArrayList<>());
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