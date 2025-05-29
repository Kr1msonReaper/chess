package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public interface GameDAO {
    Collection<GameData> gameData = new ArrayList<>();
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    void replaceGameData(GameData x, GameData newGameInfo) throws DataAccessException;
    Collection<GameData> getGames() throws DataAccessException;
    void deleteAll() throws DataAccessException;
}
