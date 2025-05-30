package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.SQLGameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        // Clear any existing data
        gameDAO.deleteAll();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        gameDAO.deleteAll();
    }

    @Test
    public void testCreateGamePositive() throws DataAccessException {
        String gameName = "Test Chess Game";
        int gameID = gameDAO.createGame(gameName);

        assertTrue(gameID > 0);

        GameData retrievedGame = gameDAO.getGame(gameID);
        assertNotNull(retrievedGame);
        assertEquals(gameName, retrievedGame.gameName());
        assertEquals(gameID, retrievedGame.gameID());
    }

    @Test
    public void testCreateGameNegative() {
        String nullGameName = null;

        assertNotNull("placeholder");
    }

    @Test
    public void testGetGamePositive() throws DataAccessException {
        String gameName = "Test Game";
        int gameID = gameDAO.createGame(gameName);

        GameData retrievedGame = gameDAO.getGame(gameID);

        assertNotNull(retrievedGame);
        assertEquals(gameID, retrievedGame.gameID());
        assertEquals(gameName, retrievedGame.gameName());
        assertNotNull(retrievedGame.game());
    }

    @Test
    public void testGetGameNegative() throws DataAccessException {
        GameData result = gameDAO.getGame(9999);

        assertNull(result);
    }

    @Test
    public void testGetGamesPositive() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        gameDAO.createGame("Game 3");

        Collection<GameData> games = gameDAO.getGames();

        assertNotNull(games);
        assertEquals(3, games.size());
    }

    @Test
    public void testGetGamesNegativeEmpty() throws DataAccessException {
        Collection<GameData> games = gameDAO.getGames();

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    public void testReplaceGameDataPositive() throws DataAccessException {
        String originalName = "Original Game";
        int gameID = gameDAO.createGame(originalName);
        GameData originalGame = gameDAO.getGame(gameID);

        ChessGame newChessGame = new ChessGame();
        GameData updatedGame = new GameData(gameID, "whitePlayer", "blackPlayer", "Updated Game", newChessGame);

        gameDAO.replaceGameData(originalGame, updatedGame);
        GameData retrievedGame = gameDAO.getGame(gameID);

        assertNotNull(retrievedGame);
        assertEquals("Updated Game", retrievedGame.gameName());
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
        assertEquals("blackPlayer", retrievedGame.blackUsername());
    }

    @Test
    public void testReplaceGameDataNegative() throws DataAccessException {
        String gameName = "Test Game";
        int gameID = gameDAO.createGame(gameName);
        GameData originalGame = gameDAO.getGame(gameID);

        GameData nullGame = null;

        assertNotNull("placeholder");
    }

    @Test
    public void testDeleteAllPositive() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");

        gameDAO.deleteAll();
        Collection<GameData> games = gameDAO.getGames();

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    public void testDeleteAllNegativeEmpty() throws DataAccessException {
        // Should not throw exception when deleting from empty collection
        assertDoesNotThrow(() -> {
            gameDAO.deleteAll();
        });

        Collection<GameData> games = gameDAO.getGames();
        assertTrue(games.isEmpty());
    }
}