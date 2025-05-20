package service;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameTest {
    private Server server;

    @BeforeEach
    public void setup() {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();

        UserData white = new UserData("whitey", "123", "w@e.com");
        UserData black = new UserData("blacky", "321", "b@e.com");

        Server.userDAO.createUser(white);
        Server.userDAO.createUser(black);

        Server.authDAO.createAuth(white);
        Server.authDAO.createAuth(black);
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void joinGameSuccess() {
        int gameId = Server.gameDAO.createGame("Championship");
        GameData game = Server.gameDAO.getGame(gameId);
        GameData updated = game.assignWhite("whitey");
        Server.gameDAO.replaceGameData(game, updated);
        assertEquals("whitey", Server.gameDAO.getGame(gameId).whiteUsername());
    }

    @Test
    public void joinGameFailureColorTaken() {
        int gameId = Server.gameDAO.createGame("Rematch");
        GameData game = Server.gameDAO.getGame(gameId);
        Server.gameDAO.replaceGameData(game, game.assignBlack("blacky"));

        GameData sameGame = Server.gameDAO.getGame(gameId);
        assertThrows(RuntimeException.class, () -> {
            if (sameGame.blackUsername() != null){ throw new RuntimeException("Color already taken");}
        });
    }
}

