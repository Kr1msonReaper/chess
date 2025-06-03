package client;

import client.ServerFacade;
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
    void logout() throws Exception {
        UserData prereqData = new UserData("player1", "password", "p1@email.com");
        AuthData data = facade.register(prereqData);
        facade.logout(data);
        assertTrue(server.authDAO.getAll().size() == 0);
        assertFalse(!server.authDAO.getAll().isEmpty());
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

    @BeforeEach
    public void clearDB() throws DataAccessException {
        server.authDAO.removeAll();
        server.gameDAO.deleteAll();
        server.userDAO.removeAll();
    }

}
