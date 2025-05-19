package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDAO implements GameDAO{

    public Collection<GameData> gameData = new ArrayList<>();

    public int createGame(AuthData data, String gameName){
        int newID = gameData.size() + 1;
        ChessGame newGame = new ChessGame();
        GameData newData = new GameData(newID, "", "", gameName, newGame);
        gameData.add(newData);
        return newID;
    }
    public Collection<ChessGame> getGames(AuthData data){

    }
    public GameData getGame(int gameID){
        for (GameData game : gameData){
            if(game.gameID() == gameID){
                return game;
            }
        }
        return null;
    }
    public int joinGame(AuthData data, ChessGame.TeamColor color, int gameID){
        GameData game = getGame(gameID);

        if(color == ChessGame.TeamColor.WHITE){
            game.whiteUsername()
        } else {

        }
    }
    public void deleteAll(){

    }

}
