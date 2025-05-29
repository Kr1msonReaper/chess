package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName) throws DataAccessException;
    Collection<GameData> getGames() throws DataAccessException;
    void deleteAll() throws DataAccessException;
}
