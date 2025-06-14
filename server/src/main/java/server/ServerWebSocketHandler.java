package server;

import chess.ChessGame;
import chess.ChessMove;
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
            sendError(session, "You can't move there!");
            return;
        }

        if (!executeMoveAndUpdate(game, cmd)) {
            sendError(session, "You can't move there!");
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
        if(!(game.game().blackInCheckmate || game.game().blackInCheck || game.game().whiteInCheckmate || game.game().whiteInCheck)){
            ServerMessage moveMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            sendEveryoneElse(session, moveMsg, moveText, cmd.getGameID());
        }


        // Send updated game state to all players AND observers with their individual perspectives
        loadGameForAllPlayers(cmd.getGameID());

        notifyCheckmate(session, game, cmd.getGameID());
    }

    private void notifyCheckmate(Session session, GameData game, int gameID) {
        String checkmateMsg = null;
        if (game.game().blackInCheck) {
            checkmateMsg = "\n" + game.blackUsername() + " is in check!";
            game.game().getPossibleMoves(ChessGame.TeamColor.BLACK);
            Collection<ChessMove> moves = game.game().possibleMoves;
            //System.out.println("\n" + game.blackUsername() + " is in check!");
        }
        if (game.game().whiteInCheck) {
            checkmateMsg = "\n" + game.whiteUsername() + " is in check!";
            game.game().getPossibleMoves(ChessGame.TeamColor.WHITE);
            Collection<ChessMove> moves = game.game().possibleMoves;
            //System.out.println("\n" + game.whiteUsername() + " is in check!");
        }

        if (game.game().blackInCheck && game.game().possibleMoves.isEmpty()) {
            checkmateMsg = "\n" + game.blackUsername() + " is in checkmate!";
            //System.out.println("\n" + game.blackUsername() + " is in checkmate!");
        }
        if (game.game().whiteInCheck && game.game().possibleMoves.isEmpty()) {
            checkmateMsg = "\n" + game.whiteUsername() + " is in checkmate!";
            //System.out.println("\n" + game.whiteUsername() + " is in checkmate!");
        }

        if (checkmateMsg != null) {
            ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            sendEveryoneElse(session, msg, checkmateMsg, gameID);
        }
    }

    private void handleResign(Session session, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());
        if (!validateResignRequest(session, cmd, auth)) {
            sendError(session, "");
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
            sendError(session, "You can't resign from that game!");
            return;
        }

        setupPlayerConnection(session, cmd, auth);

        // Load the game immediately for this connecting user (player or observer)
        loadGame(session, auth, cmd.getGameID());
    }

    private void setupPlayerConnection(Session session, UserGameCommand cmd, AuthData auth)
            throws DataAccessException {
        // First assign the game to ensure the session is in the right game map
        assignGame(session, cmd.getGameID().toString());

        String color = getPlayerColor(auth, cmd.getGameID());
        String notif;
        String role;

        if (!color.isEmpty()) {
            notif = auth.username() + " has joined the game as " + color + ".";
            role = "player";
        } else {
            notif = auth.username() + " has joined the game as an observer.";
            role = "observer";
        }

        // Assign role and auth token together to ensure proper setup
        assignRole(session, role, cmd.getAuthToken(), false, cmd.getGameID());

        // Notify other players about the new connection
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        sendEveryoneElse(session, msg, notif, cmd.getGameID());
    }

    private void handleLeave(Session session, UserGameCommand cmd) throws DataAccessException {
        AuthData auth = Server.authDAO.getAuth(cmd.getAuthToken());
        if (auth == null || !sessionExistsInGame(session, cmd.getGameID())) {
            sendError(session, "You're not a part of that game!");
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

    private void sendError(Session session, String message) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        msg.errorMessage = message;
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
        msg.auth = new AuthData(auth.authToken()
                + (getRole(auth.authToken(), gameId) == "observer" ? "observer" : ""), auth.username());
        msg.message = null;
        //System.out.println("Sent load game to: " + Server.authDAO.getAuth(getAuthTokenBySession(session, gameId)).username());

        sendToSession(session, msg);
    }

    public void loadGameForAllPlayers(int gameId) throws DataAccessException {
        GameData game = Server.gameDAO.getGame(gameId);
        if (game == null) {
            return;
        }
        getGameSessionMap(gameId).ifPresent(sessionMap -> {
            for (Map.Entry<Session, PlayerInfo> entry : sessionMap.entrySet()) {
                Session session = entry.getKey();
                PlayerInfo playerInfo = entry.getValue();
                try {
                    AuthData auth = Server.authDAO.getAuth(playerInfo.getAuthToken());
                    if (auth != null) {
                        loadGame(session, auth, gameId);
                    }
                } catch (DataAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void assignGame(Session session, String gameID) {
        Integer targetGameId = Integer.valueOf(gameID);

        // Check if there's already a session map for this game
        for (Map.Entry<ConcurrentHashMap<Session, PlayerInfo>, Integer> entry : ORGANIZED_SESSIONS.entrySet()) {
            if (entry.getValue().equals(targetGameId)) {
                // Add session with placeholder PlayerInfo - will be updated in assignRole
                entry.getKey().put(session, new PlayerInfo("", "observer", false));
                return;
            }
        }

        // Create new session map for this game if it doesn't exist
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
            if (entry.getValue() == gameID) {
                ConcurrentHashMap<Session, PlayerInfo> sessionMap = entry.getKey();
                if (sessionMap.containsKey(session)) {
                    PlayerInfo info = sessionMap.get(session);
                    if (info != null) {
                        updater.accept(info);
                    } else {
                        sessionMap.put(session, creator.get());
                    }
                    return;
                }
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

    public String getPlayerColor(AuthData token, int gameID) throws DataAccessException {
        GameData game = Server.gameDAO.getGame(gameID);
        if (game == null) {
            return "";
        }

        if (game.blackUsername() != null && game.blackUsername().equals(token.username())) {
            return "black";
        }
        if (game.whiteUsername() != null && game.whiteUsername().equals(token.username())) {
            return "white";
        }
        return "";
    }

    public static String getLetter(int num) {
        String[] letters = {"", "a", "b", "c", "d", "e", "f", "g", "h"};
        return (num >= 1 && num <= 8) ? letters[num] : "";
    }
}