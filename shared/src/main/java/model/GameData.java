package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData rename(String newName){
        return new GameData(gameID, whiteUsername, blackUsername, newName, game);
    }
    public GameData assignWhite(String newName){
        return new GameData(gameID, newName, blackUsername, gameName, game);
    }
    public GameData assignBlack(String newName){
        return new GameData(gameID, whiteUsername, newName, gameName, game);
    }
}
