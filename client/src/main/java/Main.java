import chess.ChessGame;
import chess.ChessPiece;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;
import server.Server;
import service.CreateGameRequest;
import service.JoinGameRequest;
import ui.EscapeSequences;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static ServerFacade facade;
    public static Server server = new Server();

    public static String getUnicodePiece(ChessPiece piece){
        if(piece.pieceType == ChessPiece.PieceType.PAWN){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_PAWN;
            } else {return EscapeSequences.BLACK_PAWN;}
        }
        if(piece.pieceType == ChessPiece.PieceType.ROOK){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_ROOK;
            } else {return EscapeSequences.BLACK_ROOK;}
        }
        if(piece.pieceType == ChessPiece.PieceType.KNIGHT){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_KNIGHT;
            } else {return EscapeSequences.BLACK_KNIGHT;}
        }
        if(piece.pieceType == ChessPiece.PieceType.BISHOP){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_BISHOP;
            } else {return EscapeSequences.BLACK_BISHOP;}
        }
        if(piece.pieceType == ChessPiece.PieceType.KING){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_KING;
            } else {return EscapeSequences.BLACK_KING;}
        }
        if(piece.pieceType == ChessPiece.PieceType.QUEEN){
            if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                return EscapeSequences.WHITE_QUEEN;
            } else {return EscapeSequences.BLACK_QUEEN;}
        }
        return "";
    }

    public static String drawBlackBoard(GameData data){
        String drawnBoard = "";
        String stringifiedBoard = data.game().getBoard().toString();

        for(int x = 1; x < 9; x++){
            for(int y = 8; y > 0; y--){
                ChessPiece piece = data.game().getBoard().getPosition(x, y).getPiece();
                String prettyPiece = getUnicodePiece(piece);
                if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    drawnBoard += "\u001b" + EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + prettyPiece;
                } else {
                    drawnBoard += "\u001b" + EscapeSequences.SET_BG_COLOR_WHITE + EscapeSequences.SET_TEXT_COLOR_BLACK + prettyPiece;
                }
            }
        }
        System.out.println(drawnBoard);
        return drawnBoard;
    }

    public static void main(String[] args) throws IOException {
        var port = server.run(0);
        facade = new ServerFacade(port);

        boolean isLoggedIn = false;
        UserData currentUser;
        AuthData currentToken = new AuthData("", "");

        System.out.println("♕ 240 Chess Client. Type \'Help\' to get started.");
        while(true){
            if(isLoggedIn){
                System.out.print("[LOGGED_IN] >>>");
            } else {
                System.out.print("[LOGGED_OUT] >>>");
            }
            Scanner scanner = new Scanner(System.in);
            String[] line = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");

            if(line[0].contains("help")){
                if(!isLoggedIn){
                    System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                            "login <USERNAME> <PASSWORD> - to play chess\n" +
                            "quit - playing chess\n" +
                            "help - with possible commands");
                } else {
                    System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                            "login <USERNAME> <PASSWORD> - to play chess\n" +
                            "quit - playing chess\n" +
                            "help - with possible commands\n" +
                            "logout - log out\n" +
                            "create <NAME> - create a new game.\n" +
                            "list - list existing game id's.\n" +
                            "join <GAME-ID> <DESIRED-COLOR> - join an existing game.\n" +
                            "observe <GAME-ID> - observe a game in progress.");
                }
            } else if(line[0].contains("register")){
                if(line.length == 4){
                    try{
                        UserData newUser = new UserData(line[1], line[2], line[3]);
                        currentToken = facade.register(newUser);
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
                    } catch(Exception e){
                        System.out.println("Error: " + e);
                    }

                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("login")){
                if(line.length == 3){
                    try{
                        UserData newUser = new UserData(line[1], line[2], "");
                        currentToken = facade.login(newUser);
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
                    } catch(Exception e){
                        System.out.println("Error: " + e);
                    }
                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("quit")){
                facade.logout(currentToken);
                System.out.println("Logged out");
                System.exit(0);
            } else if(line[0].contains("logout")){
                try{
                    facade.logout(currentToken);
                    isLoggedIn = false;
                    System.out.println("Logged out");
                } catch(Exception e){
                    System.out.println("Error: " + e);
                }
            } else if(line[0].contains("create")){
                CreateGameRequest req = new CreateGameRequest();
                req.gameName = line[1];
                facade.createGame(currentToken, req);
            } else if(line[0].contains("list")){
                Collection<GameData> games = facade.listGames(currentToken);
                if(games == null){continue;}
                for(GameData data : games){
                    System.out.println("[" + data.gameID() + "] " + data.gameName());
                }
            } else if(line[0].contains("join")){
                JoinGameRequest req = new JoinGameRequest();
                req.playerColor = line[2];
                req.gameID = Integer.parseInt(line[1]);
                facade.joinGame(req, currentToken);

                GameData chosenGame = new GameData(1, "", "", "", new ChessGame());
                Collection<GameData> games = facade.listGames(currentToken);
                for(GameData game : games){
                    if (game.gameID() == req.gameID){
                        chosenGame = game;
                    }
                }

                if(line[2].toLowerCase(Locale.ROOT).equals("white")){

                } else {
                    drawBlackBoard(chosenGame);
                }
            } else if(line[0].contains("observe")){

            } else {
                System.out.println("Error: Command not recognized.");
            }
        }
    }
}