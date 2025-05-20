package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName);
    Collection<GameData> getGames();
    void deleteAll();
}
