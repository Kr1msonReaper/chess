package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static server.Server.GSON;

public class SQLGameDAO implements GameDAO{

    public Collection<GameData> gameData = new ArrayList<>();

    public int createGame(String gameName) throws DataAccessException {
        int newID = gameData.size() + 1;
        ChessGame newGame = new ChessGame();
        GameData newData = new GameData(newID, "", "", gameName, newGame);
        gameData.add(newData);
        DatabaseManager.executeSQL("INSERT INTO gameData (gameDataJSON)", GSON.toJson(newData));
        return newID;
    }
    public Collection<GameData> getGames() throws DataAccessException {
        Collection<String> jsonDumps = new ArrayList<>();
        Collection<GameData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("gameData", "gameDataJSON");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, GameData.class));
        }

        return converted;
    }
    public GameData getGame(int gameID) throws DataAccessException {
        Collection<String> jsonDumps = new ArrayList<>();
        Collection<GameData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("gameData", "gameDataJSON");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, GameData.class));
        }

        for(GameData data : converted){
            if(data.gameID() == gameID){
                return data;
            }
        }

        return null;
    }

    public void replaceGameData(GameData x, GameData newGameInfo) throws DataAccessException {
        gameData.remove(x);
        gameData.add(newGameInfo);

        DatabaseManager.deleteInsertSQL("DELETE FROM gameData WHERE gameDataJSON = (?)", GSON.toJson(x));
        DatabaseManager.deleteInsertSQL("INSERT INTO gameData (gameDataJSON) VALUES (?)", GSON.toJson(newGameInfo));
    }

    public void deleteAll() throws DataAccessException {
        DatabaseManager.executeSQL("DELETE FROM gameData");
    }

}
