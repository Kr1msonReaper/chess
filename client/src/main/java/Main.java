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
        if(piece == null){
            return EscapeSequences.EMPTY;
        }
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
        return EscapeSequences.EMPTY;
    }

    public static String getLetter(int num){
        if(num == 1){
            return "a";
        }
        if(num == 2){
            return "b";
        }
        if(num == 3){
            return "c";
        }
        if(num == 4){
            return "d";
        }
        if(num == 5){
            return "e";
        }
        if(num == 6){
            return "f";
        }
        if(num == 7){
            return "g";
        }
        if(num == 8){
            return "h";
        }
        return "";
    }

    public static String drawBlackBoard(GameData data){
        String drawnBoard = "";
        String stringifiedBoard = data.game().getBoard().toString();
        Boolean isWhite = true;
        for(int x = 0; x < 10; x++){
            for(int y = 8; y > 0; y--){
                if(x == 0 || x == 9){
                    if(y == 8){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + "  ";
                    }
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_BLACK
                            + "♚" + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_COLOR_WHITE
                            + " " + getLetter(y);
                    if(y == 1){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                        drawnBoard += "\n";
                    }
                    continue;
                }
                ChessPiece piece = data.game().getBoard().getPosition(x, y).getPiece();
                String prettyPiece = getUnicodePiece(piece);

                if(y == 8){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                }

                if(isWhite){
                    drawnBoard += EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
                    isWhite = false;
                } else {
                    drawnBoard += EscapeSequences.SET_BG_COLOR_DARK_GREY;
                    isWhite = true;
                }

                if(piece == null){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + prettyPiece;
                    if(y == 1){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                        drawnBoard += "\n";
                        isWhite = !isWhite;
                    }
                    continue;
                }
                if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + prettyPiece;
                } else {
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_BLACK + prettyPiece;
                }
                if(y == 1){
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                    drawnBoard += "\n";
                    isWhite = !isWhite;
                }
            }
        }
        System.out.println(drawnBoard);
        return drawnBoard;
    }

    public static String drawWhiteBoard(GameData data){
        String drawnBoard = "";
        String stringifiedBoard = data.game().getBoard().toString();
        Boolean isWhite = true;
        for(int x = 9; x > -1; x--){
            for(int y = 1; y < 9; y++){
                if(x == 0 || x == 9){
                    if(y == 1){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + "  ";
                    }
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_BLACK
                            + "♚" + EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_COLOR_WHITE
                            + " " + getLetter(y);
                    if(y == 8){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                        drawnBoard += "\n";
                    }
                    continue;
                }
                ChessPiece piece = data.game().getBoard().getPosition(x, y).getPiece();
                String prettyPiece = getUnicodePiece(piece);

                if(y == 1){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                }

                if(isWhite){
                    drawnBoard += EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
                    isWhite = false;
                } else {
                    drawnBoard += EscapeSequences.SET_BG_COLOR_DARK_GREY;
                    isWhite = true;
                }

                if(piece == null){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + prettyPiece;
                    if(y == 8){
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                        drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                        drawnBoard += "\n";
                        isWhite = !isWhite;
                    }
                    continue;
                }
                if(piece.getTeamColor() == ChessGame.TeamColor.WHITE){
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_WHITE + prettyPiece;
                } else {
                    drawnBoard += EscapeSequences.SET_TEXT_COLOR_BLACK + prettyPiece;
                }
                if(y == 8){
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK + EscapeSequences.SET_TEXT_COLOR_WHITE + " " + x + " ";
                    drawnBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                    drawnBoard += "\n";
                    isWhite = !isWhite;
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
                    System.out.println("quit - playing chess\n" +
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
                        if(currentToken.authToken() == null){
                            Integer.parseInt("abc");
                        }
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
                    } catch(Exception e){
                        System.out.println("Error: Couldn't register, name taken.");
                    }

                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("login")){
                if(line.length == 3){
                    try{
                        UserData newUser = new UserData(line[1], line[2], "");
                        currentToken = facade.login(newUser);
                        if(currentToken.authToken() == null){
                            Integer.parseInt("abc");
                        }
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
                    } catch(Exception e){
                        System.out.println("Error: Couldn't log in.");
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
                if(line.length == 1){
                    System.out.println("Please specify a game name.");
                }
                CreateGameRequest req = new CreateGameRequest();
                req.gameName = line[1];
                facade.createGame(currentToken, req);
            } else if(line[0].contains("list")){
                Collection<GameData> games = facade.listGames(currentToken);
                if(games == null){continue;}
                int i = 1;
                for(GameData data : games){
                    System.out.println("[" + i + "] " + data.gameName() + " - Players: " + (data.whiteUsername() != null && !data.whiteUsername().isEmpty() ? data.whiteUsername() : "N/A") + " (White) & " + (data.blackUsername() != null && !data.blackUsername().isEmpty() ? data.blackUsername() : "N/A") + " (Black)");
                    i++;
                }
            } else if(line[0].contains("join")){
                if(line.length != 3){
                    System.out.println("Error: Incorrect number of arguments.");
                    continue;
                }
                JoinGameRequest req = new JoinGameRequest();
                req.playerColor = line[2];
                req.gameID = Integer.parseInt(line[1]);

                Collection<GameData> games1 = facade.listGames(currentToken);
                if(games1 == null){continue;}
                int i = 1;
                for(GameData data : games1){
                    if(i == req.gameID){
                        req.gameID = data.gameID();
                        break;
                    }
                    i++;
                }

                String result = facade.joinGame(req, currentToken);

                if(result.contains("Error")){
                    System.out.println("Error: Spot already taken or incorrect game number.");
                    continue;
                }

                GameData chosenGame = new GameData(1, "", "", "", new ChessGame());
                Collection<GameData> games = facade.listGames(currentToken);
                for(GameData game : games){
                    if (game.gameID() == req.gameID){
                        chosenGame = game;
                    }
                }

                if(line[2].toLowerCase(Locale.ROOT).equals("white")){
                    drawWhiteBoard(chosenGame);
                } else if(line[2].toLowerCase(Locale.ROOT).equals("black")){
                    drawBlackBoard(chosenGame);
                } else {
                    System.out.println("Incorrect color chosen.");
                }
            } else if(line[0].contains("observe")){

                if(line.length != 2){
                    System.out.println("Incorrect number of arguments.");
                    continue;
                }

                int id = Integer.parseInt(line[1]);

                Collection<GameData> games1 = facade.listGames(currentToken);
                if(games1 == null){continue;}
                int i = 1;
                for(GameData data : games1){
                    if(i == id){
                        id = data.gameID();
                        break;
                    }
                    i++;
                }

                GameData chosenGame = new GameData(1, "", "", "", new ChessGame());
                Collection<GameData> games = facade.listGames(currentToken);
                for(GameData game : games){
                    if (game.gameID() == id){
                        chosenGame = game;
                    }
                }
                drawWhiteBoard(chosenGame);
            } else {
                System.out.println("Error: Command not recognized.");
            }
        }
    }
}