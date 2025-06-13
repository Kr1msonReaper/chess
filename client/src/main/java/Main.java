import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import server.ServerFacade;
import javax.websocket.*;
import service.CreateGameRequest;
import service.JoinGameRequest;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class Main {
    public static ServerFacade facade;
    public static WebsocketClientHandler socket;
    public static final Gson GSON = new Gson();
    public static Main instance;
    public AuthData currentAuth;

    public static String getUnicodePiece(ChessPiece piece){
        if(piece == null){
            return EscapeSequences.EMPTY;
        }
        return getPieceByType(piece);
    }

    private static String getPieceByType(ChessPiece piece) {
        switch(piece.pieceType) {
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KING:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                        EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            default:
                return EscapeSequences.EMPTY;
        }
    }

    public static String getLetter(int num){
        String[] letters = {"", "a", "b", "c", "d", "e", "f", "g", "h"};
        return (num >= 1 && num <= 8) ? letters[num] : "";
    }

    public static int getNumber(String letter){
        List<String> letters = new ArrayList<>();
        letters.addAll(Arrays.asList("", "a", "b", "c", "d", "e", "f", "g", "h"));
        return letters.indexOf(letter);
    }

    public static String drawWhiteBoard(GameData data, Collection<ChessMove> possibleMoves){
        return drawBoard(data, true, possibleMoves);
    }

    public static String drawBlackBoard(GameData data, Collection<ChessMove> possibleMoves){
        return drawBoard(data, false, possibleMoves);
    }

    private static String drawBoard(GameData data, boolean isWhitePerspective, Collection<ChessMove> possibleMoves) {
        StringBuilder drawnBoard = new StringBuilder();
        Boolean isWhite = true;

        int startX = isWhitePerspective ? 9 : 0;
        int endX = isWhitePerspective ? -1 : 10;
        int xIncrement = isWhitePerspective ? -1 : 1;

        for(int x = startX; x != endX; x += xIncrement){
            isWhite = drawRow(data, drawnBoard, x, isWhite, isWhitePerspective, possibleMoves);
        }

        String result = drawnBoard.toString();
        System.out.println(result);
        return result;
    }

    private static Boolean drawRow(GameData data, StringBuilder drawnBoard, int x, Boolean isWhite, boolean isWhitePerspective, Collection<ChessMove> possibleMoves) {
        int startY = isWhitePerspective ? 1 : 8;
        int endY = isWhitePerspective ? 9 : 0;
        int yIncrement = isWhitePerspective ? 1 : -1;

        for(int y = startY; y != endY; y += yIncrement){
            if(x == 0 || x == 9){
                drawBorder(drawnBoard, y, isWhitePerspective);
                continue;
            }

            ChessPiece piece = data.game().getBoard().getPosition(x, y).getPiece();
            String prettyPiece = getUnicodePiece(piece);

            int firstY = isWhitePerspective ? 1 : 8;
            if(y == firstY){
                drawnBoard.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(" ").append(x).append(" ");
            }

            isWhite = drawSquare(drawnBoard, piece, prettyPiece, isWhite, y, x, isWhitePerspective, possibleMoves);
        }
        return isWhite;
    }

    private static void drawBorder(StringBuilder drawnBoard, int y, boolean isWhitePerspective) {
        int firstY = isWhitePerspective ? 1 : 8;
        int lastY = isWhitePerspective ? 8 : 1;

        if(y == firstY){
            drawnBoard.append(EscapeSequences.SET_BG_COLOR_BLACK)
                    .append(EscapeSequences.SET_TEXT_COLOR_WHITE)
                    .append("  ");
        }
        drawnBoard.append(EscapeSequences.SET_BG_COLOR_BLACK)
                .append(EscapeSequences.SET_TEXT_COLOR_BLACK)
                .append("♚")
                .append(EscapeSequences.SET_TEXT_COLOR_WHITE)
                .append(EscapeSequences.SET_TEXT_COLOR_WHITE)
                .append(" ")
                .append(getLetter(y));
        if(y == lastY){
            drawnBoard.append(EscapeSequences.SET_BG_COLOR_BLACK).append("\n");
        }
    }

    private static Boolean drawSquare(StringBuilder drawnBoard, ChessPiece piece, String prettyPiece,
                                      Boolean isWhite, int y, int x, boolean isWhitePerspective, Collection<ChessMove> possibleMoves) {
        boolean isListed = false;
        for(ChessMove pos : possibleMoves){
            if(pos.getEndPosition().x == x && pos.getEndPosition().y == y
            || pos.getStartPosition().x == x && pos.getStartPosition().y == y){
                isListed = true;
            }
        }
        if(isWhite){
            if(!isListed) {drawnBoard.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);}else{drawnBoard.append(EscapeSequences.SET_BG_COLOR_YELLOW);}
            isWhite = false;
        } else {
            if(!isListed) {drawnBoard.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);}else{drawnBoard.append(EscapeSequences.SET_BG_COLOR_YELLOW);}
            isWhite = true;
        }

        if(piece == null){
            drawnBoard.append(EscapeSequences.SET_TEXT_COLOR_WHITE).append(prettyPiece);
        } else {
            String textColor = piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                    EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK;
            drawnBoard.append(textColor).append(prettyPiece);
        }

        int lastY = isWhitePerspective ? 8 : 1;
        if(y == lastY){
            drawnBoard.append(EscapeSequences.SET_BG_COLOR_BLACK)
                    .append(EscapeSequences.SET_TEXT_COLOR_WHITE)
                    .append(" ").append(x).append(" ")
                    .append(EscapeSequences.SET_BG_COLOR_BLACK)
                    .append("\n");
            isWhite = !isWhite;
        }

        return isWhite;
    }

    public static void main(String[] args) throws IOException, DeploymentException {
        facade = new ServerFacade(8080);
        WebSocketContainer socketContainer = ContainerProvider.getWebSocketContainer();
        socket = new WebsocketClientHandler();
        socketContainer.connectToServer(socket, URI.create("ws://localhost:8080/ws"));
        boolean isLoggedIn = false;
        boolean isInGame = false;
        UserData currentUser;
        AuthData currentToken = new AuthData("", "");

        System.out.println("♕ 240 Chess Client. Type \'Help\' to get started.");
        while(true){
            String[] line = getInputLine(isLoggedIn);
            processCommand(line, isLoggedIn, currentToken);

            CommandResult result = executeCommand(line, isLoggedIn, isInGame, currentToken);
            try {
                isLoggedIn = result.isLoggedIn;
                isInGame = result.isInGame;
                currentToken = result.authToken;
                if (result.shouldExit) {
                    break;
                }
            } catch(Exception e){

            }
        }
    }

    private static String[] getInputLine(boolean isLoggedIn) {
        if(isLoggedIn){
            System.out.print("[LOGGED_IN] >>>");
        } else {
            System.out.print("[LOGGED_OUT] >>>");
        }
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");
    }

    private static void processCommand(String[] line, boolean isLoggedIn, AuthData currentToken) {
    }

    private static CommandResult executeCommand(String[] line, boolean isLoggedIn, boolean isInGame, AuthData currentToken) throws IOException {
        CommandResult result = new CommandResult(isLoggedIn, isInGame, currentToken, false);

        if(line[0].contains("help")){
            handleHelpCommand(isLoggedIn, isInGame);
        } else if(line[0].contains("register") && !isInGame){
            result = handleRegisterCommand(line);
        } else if(line[0].contains("login") && !isInGame){
            result = handleLoginCommand(line);
        } else if(line[0].contains("quit")){
            handleQuitCommand(currentToken);
            result.shouldExit = true;
        } else if(line[0].contains("logout") && isLoggedIn){
            result = handleLogoutCommand(currentToken);
        } else if(line[0].contains("create") && isLoggedIn){
            handleCreateCommand(line, currentToken);
        } else if(line[0].contains("list") && isLoggedIn){
            handleListCommand(currentToken);
        } else if(line[0].contains("join") && isLoggedIn && !isInGame){
            result = handleJoinCommand(line, currentToken);
        } else if(line[0].contains("observe") && isLoggedIn && !isInGame) {
            handleObserveCommand(line, currentToken);
        } else if(line[0].contains("redraw") && line[1].contains("chess") && line[2].contains("board") && isInGame){
            redrawBoard(currentToken, null, -1, -1);
        } else if(line[0].contains("leave") && isInGame){
            return handleLeaveCommand(currentToken);
        } else if(line[0].contains("make") && line[1].contains("move") && isInGame){
            handleMakeMove(line, currentToken);
        } else if(line[0].contains("resign") && isInGame){

        } else if(line[0].contains("highlight") && line[1].contains("legal") && line[2].contains("moves") && isInGame){
            redrawBoard(currentToken, null, Integer.parseInt(line[3]), getNumber(line[4]));
        } else {
            System.out.println("Error: Command not recognized.");
        }

        return result;
    }

    private static void handleMakeMove(String[] line, AuthData token) throws IOException {
        Collection<GameData> games = facade.listGames(token);
        Integer gameID = -1;
        for(GameData game : games){
            if(game.blackUsername() != null && game.blackUsername().equals(token.username())){
                gameID = game.gameID();
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(token.username())){
                gameID = game.gameID();
            }
        }
        int x1 = Integer.parseInt(line[2]);
        int y1 = getNumber(line[3]);
        int x2 = Integer.parseInt(line[4]);
        int y2 = getNumber(line[5]);
        ChessPosition start = new ChessPosition(x1, y1);
        ChessPosition end = new ChessPosition(x2, y2);
        ChessMove newMove = new ChessMove(start, end, null);
        UserGameCommand newMessage = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, token.authToken(), gameID);
        newMessage.move = newMove;
        socket.sendMessage(GSON.toJson(newMessage, UserGameCommand.class));
    }

    private static CommandResult handleLeaveCommand(AuthData token) throws IOException {
        Collection<GameData> games = facade.listGames(token);
        Integer gameID = -1;
        for(GameData game : games){
            if(game.blackUsername() != null && game.blackUsername().equals(token.username())){
                gameID = game.gameID();
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(token.username())){
                gameID = game.gameID();
            }
        }

        UserGameCommand newMsg = new UserGameCommand(UserGameCommand.CommandType.LEAVE, token.authToken(), gameID);
        socket.sendMessage(GSON.toJson(newMsg));
        CommandResult result = new CommandResult(true, false, token, false);
        return result;
    }

    private static void handleHelpCommand(boolean isLoggedIn, boolean isInGame) {
        if(isInGame){
            System.out.println("help - discover what actions you can take.\n" +
                    "redraw chess board - view the current board.\n" +
                    "leave - leave the game.\n" +
                    "make move <1-8> <a-h> to <1-8> <a-h> - move a piece.\n" +
                    "resign - forfeit the game.\n" +
                    "highlight legal moves <1-8> <a-h> - view possible moves.\n" +
                    "help - with possible commands");
            return;
        }
        if(!isLoggedIn){
            System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                    "login <USERNAME> <PASSWORD> - to play chess\n" +
                    "quit - playing chess\n" +
                    "help - with possible commands");
        } else {
            System.out.println("quit - playing chess\n" +
                    "help - with possible commands\n" +
                    "logout - log out\n" +
                    "create <NAME> - create a new game.\n" +
                    "list - list existing game id's.\n" +
                    "join <GAME-ID> <DESIRED-COLOR> - join an existing game.\n" +
                    "observe <GAME-ID> - observe a game in progress.");
        }
    }

    private static CommandResult handleRegisterCommand(String[] line) {
        if(line.length != 4){
            System.out.println("Error: Incorrect number of arguments.");
            return new CommandResult(false, false, new AuthData("", ""), false);
        }

        try{
            UserData newUser = new UserData(line[1], line[2], line[3]);
            AuthData token = facade.register(newUser);
            if(token.authToken() == null){
                Integer.parseInt("abc");
            }
            System.out.println("Logged in as " + line[1]);
            socket.sendMessage("Identification:" + token.authToken());
            return new CommandResult(true, false, token, false);
        } catch(Exception e){
            System.out.println("Error: Couldn't register, name taken.");
            return new CommandResult(false, false, new AuthData("", ""), false);
        }
    }

    private static CommandResult handleLoginCommand(String[] line) {
        if(line.length != 3){
            System.out.println("Error: Incorrect number of arguments.");
            return new CommandResult(false, false, new AuthData("", ""), false);
        }

        try{
            UserData newUser = new UserData(line[1], line[2], "");
            AuthData token = facade.login(newUser);
            if(token.authToken() == null){
                Integer.parseInt("abc");
            }
            System.out.println("Logged in as " + line[1]);
            socket.sendMessage("Identification:" + token.authToken());
            return new CommandResult(true, false, token, false);
        } catch(Exception e){
            System.out.println("Error: Couldn't log in.");
            return new CommandResult(false, false, new AuthData("", ""), false);
        }
    }

    private static void handleQuitCommand(AuthData currentToken) throws IOException {
        facade.logout(currentToken);
        System.out.println("Logged out");
        System.exit(0);
    }

    private static CommandResult handleLogoutCommand(AuthData currentToken) {
        try{
            facade.logout(currentToken);
            System.out.println("Logged out");
            return new CommandResult(false, false, new AuthData("", ""), false);
        } catch(Exception e){
            System.out.println("Error: " + e);
            return new CommandResult(true, false, currentToken, false);
        }
    }

    private static void handleCreateCommand(String[] line, AuthData currentToken) throws IOException {
        if(line.length == 1){
            System.out.println("Please specify a game name.");
            return;
        }
        CreateGameRequest req = new CreateGameRequest();
        req.gameName = line[1];
        facade.createGame(currentToken, req);
    }

    private static void handleListCommand(AuthData currentToken) throws IOException {
        Collection<GameData> games = facade.listGames(currentToken);
        if(games == null) {return;}

        int i = 1;
        for(GameData data : games){
            String whiteName = (data.whiteUsername() != null && !data.whiteUsername().isEmpty()) ?
                    data.whiteUsername() : "N/A";
            String blackName = (data.blackUsername() != null && !data.blackUsername().isEmpty()) ?
                    data.blackUsername() : "N/A";
            System.out.println("[" + i + "] " + data.gameName() + " - Players: " +
                    whiteName + " (White) & " + blackName + " (Black)");
            i++;
        }
    }

    public static void redrawBoard(AuthData currentToken, GameData passedGame, int x, int y) throws IOException {
        Collection<GameData> games = facade.listGames(currentToken);
        Collection<ChessMove> filteredMoves = new ArrayList<>();

        for(GameData game : games){
            if(passedGame != null){
                game = passedGame;
            }
            if(game.blackUsername() != null && game.blackUsername().equals(currentToken.username())){
                game.game().getPossibleMoves(ChessGame.TeamColor.BLACK);
                for(ChessMove move : game.game().possibleMoves){
                    if(x == -1 && y == -1){
                        break;
                    }
                    if(move.getStartPosition().x == x && move.getStartPosition().y == y){
                        filteredMoves.add(move);
                    }
                }

                drawBlackBoard(game, filteredMoves);
                break;
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(currentToken.username())){
                game.game().getPossibleMoves(ChessGame.TeamColor.WHITE);
                for(ChessMove move : game.game().possibleMoves){
                    if(x == -1 && y == -1){
                        break;
                    }
                    if(move.getStartPosition().x == x && move.getStartPosition().y == y){
                        filteredMoves.add(move);
                    }
                }

                drawWhiteBoard(game, filteredMoves);
                break;
            }
        }
    }

    private static CommandResult handleJoinCommand(String[] line, AuthData currentToken) throws IOException {
        if(line.length != 3){
            System.out.println("Error: Incorrect number of arguments.");
            return null;
        }

        JoinGameRequest req = createJoinRequest(line, currentToken);
        if(req == null) {return null;}

        String result = facade.joinGame(req, currentToken);
        if(result.contains("Error")){
            System.out.println("Error: Spot already taken, incorrect game number, or unrecognizable color.");
            return null;
        }

        GameData chosenGame = findGameById(req.gameID, currentToken);
        //displayGameBoard(line[2], chosenGame);
        socket.sendMessage("AssignGame:" + req.gameID);
        sendJoinMessage(currentToken.authToken(), req.gameID, "");
        //System.out.println(currentToken.authToken());
        return new CommandResult(true, true, currentToken, false);
    }

    private static void sendJoinMessage(String currentToken, Integer gameID, String message){
        UserGameCommand newCmd = new UserGameCommand(UserGameCommand.CommandType.CONNECT, currentToken, gameID, message);
        socket.sendMessage(GSON.toJson(newCmd, UserGameCommand.class));
    }

    private static JoinGameRequest createJoinRequest(String[] line, AuthData currentToken) throws IOException {
        JoinGameRequest req = new JoinGameRequest();
        req.playerColor = line[2];
        try {
            req.gameID = Integer.parseInt(line[1]);
        } catch(Exception e){
            System.out.println("Error: Incorrect game number.");
            return null;
        }

        Collection<GameData> games = facade.listGames(currentToken);
        if(games == null) {return null;}

        int i = 1;
        for(GameData data : games){
            if(i == req.gameID){
                req.gameID = data.gameID();
                break;
            }
            i++;
        }
        return req;
    }

    private static GameData findGameById(int gameId, AuthData currentToken) throws IOException {
        Collection<GameData> games = facade.listGames(currentToken);
        for(GameData game : games){
            if (game.gameID() == gameId){
                return game;
            }
        }
        return new GameData(1, "", "", "", new ChessGame());
    }

    private static void displayGameBoard(String color, GameData chosenGame) {
        String lowerColor = color.toLowerCase(Locale.ROOT);
        if(lowerColor.equals("white")){
            drawWhiteBoard(chosenGame, new ArrayList<>());
        } else if(lowerColor.equals("black")){
            drawBlackBoard(chosenGame, new ArrayList<>());
        } else {
            System.out.println("Incorrect color chosen.");
        }
    }

    private static void handleObserveCommand(String[] line, AuthData currentToken) throws IOException {
        if(line.length != 2){
            System.out.println("Incorrect number of arguments.");
            return;
        }

        int id = 0;
        try {
            id = Integer.parseInt(line[1]);
        } catch(Exception e){
            System.out.println("Error: Incorrect game number.");
            return;
        }

        int gameId = findGameIdFromList(id, currentToken);
        if(gameId == -1){
            System.out.println("Game does not exist.");
            return;
        }

        GameData chosenGame = findGameById(gameId, currentToken);
        socket.sendMessage("AssignGame:" + gameId);
        sendJoinMessage(currentToken.authToken(), gameId, "observer");
        //drawWhiteBoard(chosenGame, new ArrayList<>());
    }

    private static int findGameIdFromList(int listIndex, AuthData currentToken) throws IOException {
        Collection<GameData> games = facade.listGames(currentToken);
        if(games == null) {return -1;}

        int i = 1;
        for(GameData data : games){
            if(i == listIndex){
                return data.gameID();
            }
            i++;
        }
        return -1;
    }

    private static class CommandResult {
        boolean isLoggedIn;
        boolean isInGame;
        AuthData authToken;
        boolean shouldExit;

        CommandResult(boolean isLoggedIn, boolean isInGame, AuthData authToken, boolean shouldExit) {
            this.isLoggedIn = isLoggedIn;
            this.isInGame = isInGame;
            this.authToken = authToken;
            this.shouldExit = shouldExit;
        }
    }
}