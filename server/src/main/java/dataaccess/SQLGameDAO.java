package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

import static server.Server.GSON;

public class SQLGameDAO implements GameDAO {

    private final Collection<GameData> gameData = new ArrayList<>();

    @Override
    public int createGame(String gameName) throws DataAccessException {
        try {
            int newID = gameData.size() + 1;
            ChessGame newGame = new ChessGame();
            GameData newData = new GameData(newID, "", "", gameName, newGame);
            gameData.add(newData);

            DatabaseManager.deleteInsertSQL("INSERT INTO gameData (gameDataJSON) VALUES (?)", GSON.toJson(newData));
            return newID;
        } catch (Exception e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    @Override
    public Collection<GameData> getGames() throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("gameData", "gameDataJSON");
            Collection<GameData> converted = new ArrayList<>();

            for (String dump : jsonDumps) {
                converted.add(GSON.fromJson(dump, GameData.class));
            }

            return converted;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get games", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("gameData", "gameDataJSON");

            for (String dump : jsonDumps) {
                GameData data = GSON.fromJson(dump, GameData.class);
                if (data.gameID() == gameID) {
                    return data;
                }
            }

            return null;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get game with ID " + gameID, e);
        }
    }

    @Override
    public void replaceGameData(GameData oldGame, GameData newGameInfo) throws DataAccessException {
        try {
            if (!gameData.contains(oldGame)) {
                return;
            }
            gameData.remove(oldGame);
            gameData.add(newGameInfo);

            String likeClause = "\"gameID\": " + oldGame.gameID();
            DatabaseManager.deleteIfLikeSQL("DELETE FROM gameData WHERE gameDataJSON LIKE ?", likeClause);
            DatabaseManager.deleteInsertSQL("INSERT INTO gameData (gameDataJSON) VALUES (?)", GSON.toJson(newGameInfo));
        } catch (Exception e) {
            throw new DataAccessException("Failed to replace game data for gameID " + oldGame.gameID(), e);
        }
    }

    @Override
    public void deleteAll() throws DataAccessException {
        try {
            DatabaseManager.executeSQL("DELETE FROM gameData");
        } catch (Exception e) {
            throw new DataAccessException("Failed to delete all games", e);
        }
    }
}
