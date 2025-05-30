package service;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTest {
    private Server server;

    @BeforeEach
    public void setup() throws DataAccessException {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();
        Server.userDAO.removeAll();
        Server.gameDAO.deleteAll();
        Server.authDAO.removeAll();

        UserData user = new UserData("viewer", "pass", "email@a.com");
        Server.userDAO.createUser(user);
        Server.authDAO.createAuth(user);

        Server.gameDAO.createGame("Game A");
        Server.gameDAO.createGame("Game B");
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        List<GameData> games = Server.gameDAO.getGames().stream().toList();
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game A")));
    }

    @Test
    public void listGamesFailureUnauthorized() throws DataAccessException {
        String token = "invalidToken";
        assertNull(Server.authDAO.getAuth(token));
    }
}

