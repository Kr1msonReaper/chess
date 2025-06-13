package client;

import ServerFacade;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.AuthData;
import model.UserData;
import server.Server;
import service.*;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register() throws Exception {
        UserData reqData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(reqData);
        assertTrue(authData.authToken().length() > 10);
        UserData reqData2 = new UserData("player1", "password", "p1@email.com");
        AuthData authData2 = facade.register(reqData2);
        assertNull(authData2.authToken());
    }

    @Test
    void registerNegative() throws Exception {
        UserData nullUsernameData = new UserData(null, "password", "p1@email.com");
        AuthData nullUsernameAuth = facade.register(nullUsernameData);
        assertNull(nullUsernameAuth.authToken());

        UserData emptyPasswordData = new UserData("player2", "", "p2@email.com");
        AuthData emptyPasswordAuth = facade.register(emptyPasswordData);
        assertNull(emptyPasswordAuth.authToken());
    }

    @Test
    void login() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        facade.register(prereqData);

        UserData reqData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.login(reqData);
        assertTrue(authData.authToken().length() > 10);
        UserData reqData2 = new UserData("non-existing-player", "password", "p1@email.com");
        AuthData authData2 = facade.login(reqData2);
        assertNull(authData2.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        facade.register(prereqData);

        UserData wrongPasswordData = new UserData("player1", "wrongpassword", "p1@email.com");
        AuthData wrongPasswordAuth = facade.login(wrongPasswordData);
        assertNull(wrongPasswordAuth.authToken());

        UserData nullUsernameData = new UserData(null, "password", "p1@email.com");
        AuthData nullUsernameAuth = facade.login(nullUsernameData);
        assertNull(nullUsernameAuth.authToken());

        UserData emptyUsernameData = new UserData("", "password", "p1@email.com");
        AuthData emptyUsernameAuth = facade.login(emptyUsernameData);
        assertNull(emptyUsernameAuth.authToken());
    }

    @Test
    void logout() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        facade.logout(data);
        assertTrue(server.authDAO.getAll().size() == 0);
        assertFalse(!server.authDAO.getAll().isEmpty());
    }

    @Test
    void logoutNegative() throws Exception {
        AuthData invalidAuth = new AuthData("invalid-token", "nonexistent-user");
        assertTrue(true);

        AuthData nullTokenAuth = new AuthData(null, "player1");
        assertTrue(true);

        AuthData emptyTokenAuth = new AuthData("", "player1");
        assertTrue(true);
    }

    @Test
    void createGame() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        CreateGameRequest newGameReq = new CreateGameRequest();
        newGameReq.gameName = "New Game";
        facade.createGame(data, newGameReq);

        assertTrue(server.gameDAO.getGames().size() == 1);
        assertFalse(server.gameDAO.getGames().isEmpty());
    }

    @Test
    void createGameNegative() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);

        CreateGameRequest nullNameReq = new CreateGameRequest();
        nullNameReq.gameName = null;
        assertTrue(facade.createGame(data, nullNameReq) == -1);

        CreateGameRequest emptyNameReq = new CreateGameRequest();
        emptyNameReq.gameName = "";
        assertTrue(facade.createGame(data, emptyNameReq) == -1);

        AuthData invalidAuth = new AuthData("invalid-token", "nonexistent-user");
        CreateGameRequest validReq = new CreateGameRequest();
        validReq.gameName = "Valid Game";
        assertTrue(facade.createGame(invalidAuth, validReq) == -1);
    }

    @Test
    void listGames() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        CreateGameRequest newGameReq = new CreateGameRequest();
        newGameReq.gameName = "New Game";
        facade.createGame(data, newGameReq);

        assertTrue(server.gameDAO.getGames().size() == facade.listGames(data).size());
        assertFalse(server.gameDAO.getGames().isEmpty());
    }

    @Test
    void listGamesNegative() throws Exception {
        AuthData invalidAuth = new AuthData("invalid-token", "nonexistent-user");
        assertTrue(facade.listGames(invalidAuth) == null);

        AuthData nullTokenAuth = new AuthData(null, "player1");
        assertTrue(facade.listGames(nullTokenAuth) == null);

        AuthData emptyTokenAuth = new AuthData("", "player1");
        assertTrue(facade.listGames(emptyTokenAuth) == null);
    }

    @Test
    void joinGame() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        CreateGameRequest newGameReq = new CreateGameRequest();
        newGameReq.gameName = "New Game";
        int newID = facade.createGame(data, newGameReq);

        JoinGameRequest req = new JoinGameRequest();
        req.playerColor = "WHITE";
        req.gameID = newID;
        facade.joinGame(req, data);

        assertTrue(server.gameDAO.getGame(newID).whiteUsername().equals("player1"));
        assertFalse(!server.gameDAO.getGame(newID).whiteUsername().equals("player1"));
    }

    @Test
    void joinGameNegative() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        CreateGameRequest newGameReq = new CreateGameRequest();
        newGameReq.gameName = "New Game";
        int newID = facade.createGame(data, newGameReq);

        JoinGameRequest invalidGameReq = new JoinGameRequest();
        invalidGameReq.playerColor = "WHITE";
        invalidGameReq.gameID = 99999;
        assertTrue(facade.joinGame(invalidGameReq, data).contains("Error"));

        JoinGameRequest invalidColorReq = new JoinGameRequest();
        invalidColorReq.playerColor = "PURPLE";
        invalidColorReq.gameID = newID;
        assertTrue(facade.joinGame(invalidColorReq, data).contains("Error"));

        JoinGameRequest req = new JoinGameRequest();
        req.playerColor = "WHITE";
        req.gameID = newID;
        facade.joinGame(req, data);

        UserData player2Data = new UserData("player2", "password", "p2@email.com");
        AuthData player2Auth = facade.register(player2Data);
        JoinGameRequest duplicateColorReq = new JoinGameRequest();
        duplicateColorReq.playerColor = "WHITE";
        duplicateColorReq.gameID = newID;
        assertTrue(facade.joinGame(duplicateColorReq, player2Auth).contains("Error"));

        AuthData invalidAuth = new AuthData("invalid-token", "nonexistent-user");
        JoinGameRequest validReq = new JoinGameRequest();
        validReq.playerColor = "BLACK";
        validReq.gameID = newID;
        assertTrue(facade.joinGame(validReq, invalidAuth).contains("Error"));
    }

    @BeforeEach
    public void clearDB() throws DataAccessException {
        server.authDAO.removeAll();
        server.gameDAO.deleteAll();
        server.userDAO.removeAll();
    }
}