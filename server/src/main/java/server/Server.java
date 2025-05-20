package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.JoinGameRequest;
import service.ListGamesResult;
import spark.*;

import java.util.Objects;

public class Server {

    public static MemoryAuthDAO authDAO;
    public static MemoryGameDAO gameDAO;
    public static MemoryUserDAO userDAO;

    private static final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();

        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/user", this::register);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDB);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object login(Request req, Response res) {
        UserData loginReq;
        try {
            loginReq = gson.fromJson(req.body(), UserData.class);
            if (isBlank(loginReq.username()) || isBlank(loginReq.password())) throw new Exception();
        } catch (Exception e) {
            return error(res, 400, "bad request");
        }

        if (!userDAO.userExists(loginReq)) return error(res, 401, "user doesn't exist");

        UserData existingUser = userDAO.getUser(loginReq.username());
        if (!existingUser.password().equals(loginReq.password())) {
            return error(res, 401, "unauthorized");
        }

        AuthData authInfo = authDAO.createAuth(existingUser);
        res.type("application/json");
        return gson.toJson(authInfo);
    }

    private Object logout(Request req, Response res) {
        String token = req.headers("authorization");
        if (authDAO.getAuth(token) != null) {
            authDAO.removeAuth(token);
            return success(res, 200);
        }
        return error(res, 401, "unauthorized");
    }

    private Object register(Request req, Response res) {
        UserData registerReq;
        try {
            registerReq = gson.fromJson(req.body(), UserData.class);
            if (isBlank(registerReq.username()) || isBlank(registerReq.password())) throw new Exception();
        } catch (Exception e) {
            return error(res, 400, "bad request");
        }

        if (userDAO.userExists(registerReq)) return error(res, 403, "already taken");

        userDAO.createUser(registerReq);
        AuthData authInfo = authDAO.createAuth(registerReq);

        res.type("application/json");
        return gson.toJson(authInfo);
    }

    private Object listGames(Request req, Response res) {
        if (!authorized(req, res)) return error(res, 401, "unauthorized");

        ListGamesResult result = new ListGamesResult();
        result.games = gameDAO.getGames();

        res.type("application/json");
        return gson.toJson(result);
    }

    private Object createGame(Request req, Response res) {
        if (!authorized(req, res)) return error(res, 401, "unauthorized");

        CreateGameRequest gameReq = gson.fromJson(req.body(), CreateGameRequest.class);
        if (isBlank(gameReq.gameName)) return error(res, 400, "bad request");

        int gameID = gameDAO.createGame(gameReq.gameName);
        CreateGameResult result = new CreateGameResult();
        result.gameID = gameID;

        res.type("application/json");
        return gson.toJson(result);
    }

    private Object joinGame(Request req, Response res) {
        if (!authorized(req, res)) return error(res, 401, "unauthorized");

        JoinGameRequest joinReq = gson.fromJson(req.body(), JoinGameRequest.class);
        String token = req.headers("authorization");
        String username = authDAO.getAuth(token).username();

        GameData game = gameDAO.getGame(joinReq.gameID);
        String color = joinReq.playerColor;

        if (game == null || (!"WHITE".equals(color) && !"BLACK".equals(color))) {
            return error(res, 400, "bad request");
        }

        if ("WHITE".equals(color) && !isBlank(game.whiteUsername())) {
            return error(res, 403, "already taken");
        }
        if ("BLACK".equals(color) && !isBlank(game.blackUsername())) {
            return error(res, 403, "already taken");
        }

        // Assign player to color
        GameData updatedGame = "WHITE".equals(color)
                ? game.assignWhite(username)
                : game.assignBlack(username);
        gameDAO.replaceGameData(game, updatedGame);

        // Reassign null to empty fields (exact behavior match)
        GameData refreshed = gameDAO.getGame(joinReq.gameID);
        if ("".equals(refreshed.whiteUsername())) {
            gameDAO.replaceGameData(refreshed, refreshed.assignWhite(null));
        }
        if ("".equals(refreshed.blackUsername())) {
            gameDAO.replaceGameData(refreshed, refreshed.assignBlack(null));
        }

        return success(res, 200);
    }

    private Object clearDB(Request req, Response res) {
        try {
            userDAO.removeAll();
            authDAO.removeAll();
            gameDAO.deleteAll();
        } catch (Exception e) {
            return error(res, 500, e.getMessage());
        }
        return success(res, 200);
    }

    private boolean authorized(Request req, Response res) {
        String token = req.headers("authorization");
        if (token == null || authDAO.getAuth(token) == null) {
            error(res, 401, "unauthorized");
            return false;
        }
        return true;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String error(Response res, int status, String message) {
        res.type("application/json");
        res.status(status);
        return String.format("{\"message\": \"Error: (%s)\"}", message);
    }

    private String success(Response res, int status) {
        res.type("application/json");
        res.status(status);
        return String.format("{\"message\": %d}", status);
    }
}
