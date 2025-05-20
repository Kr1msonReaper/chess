package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.JoinGameRequest;
import service.ListGamesResult;
import model.UserData;
import spark.*;

import java.util.Objects;

public class Server {

    public static MemoryAuthDAO authDAO;
    public static MemoryGameDAO gameDAO;
    public static MemoryUserDAO userDAO;


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        var serializer = new Gson();

        // Register your endpoints and handle exceptions here.

        Spark.post("/session", (req, res) -> {
            //Login
            UserData loginReq = new UserData("", "", "");
            try {
                loginReq = serializer.fromJson(req.body(), UserData.class);
                if(loginReq.username() == null || loginReq.password() == null){int i = 37 / 0;}
            } catch(Exception e){
                res.type("application/json");
                res.status(400);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "bad request"));
            }
            if(!userDAO.userExists(loginReq)){
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "user doesn't exist"));
            }

            UserData existingUser = userDAO.getUser(loginReq.username());

            if(!existingUser.password().equals(loginReq.password())){
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "unauthorized"));
            }

            AuthData authInfo = authDAO.createAuth(existingUser);

            var json = serializer.toJson(authInfo);

            res.type("application/json");
            return json;
        });

        Spark.delete("/session", (req, res) -> {
            //Logout
            String token = req.headers("authorization");
            if(authDAO.getAuth(token) != null){
                authDAO.removeAuth(token);
                res.type("application/json");
                return String.format("{\"message\": %d}", 200);
            } else {
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "unauthorized"));
            }
        });

        Spark.get("/game", (req, res) -> {
            //List games
            String token = req.headers("authorization");
            if(authDAO.getAuth(token) != null){

                ListGamesResult payload = new ListGamesResult();
                payload.games = gameDAO.getGames();

                var json = serializer.toJson(payload);

                res.type("application/json");
                return json;
            } else {
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "unauthorized"));
            }
        });

        Spark.post("/game", (req, res) -> {
            //Create a game!
            String token = req.headers("authorization");
            if(authDAO.getAuth(token) != null){

                CreateGameRequest reqObj = serializer.fromJson(req.body(), CreateGameRequest.class);

                if(reqObj.gameName == null || reqObj.gameName.equals("")){
                    res.type("application/json");
                    res.status(400);
                    return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "bad request"));
                }

                int gameID = gameDAO.createGame(reqObj.gameName);
                CreateGameResult payload = new CreateGameResult();
                payload.gameID = gameID;

                var json = serializer.toJson(payload);

                res.type("application/json");
                return json;
            } else {
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "unauthorized"));
            }
        });

        Spark.put("/game", (req, res) -> {
            // Join game!
            String token = req.headers("authorization");

            if(authDAO.getAuth(token) != null){

                JoinGameRequest reqObj = serializer.fromJson(req.body(), JoinGameRequest.class);
                String username = authDAO.getAuth(token).username();
                GameData joinedGame = gameDAO.getGame(reqObj.gameID);

                if(joinedGame == null || reqObj.playerColor == null
                        || !(reqObj.playerColor.equals("BLACK") || reqObj.playerColor.equals("WHITE"))){
                    res.type("application/json");
                    res.status(400);
                    return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "bad request"));
                }

                if(reqObj.playerColor.equals("WHITE")){
                    if(!Objects.equals(joinedGame.whiteUsername(), "")
                            && !Objects.equals(joinedGame.whiteUsername(), null)){
                        res.type("application/json");
                        res.status(403);
                        return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "already taken"));
                    }
                }

                if(reqObj.playerColor.equals("BLACK")){
                    if(!Objects.equals(joinedGame.blackUsername(), "")
                            && !Objects.equals(joinedGame.blackUsername(), null)){
                        res.type("application/json");
                        res.status(403);
                        return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "already taken"));
                    }
                }

                if(reqObj.playerColor.equals("WHITE")){
                    GameData newData = joinedGame.assignWhite(username);
                    gameDAO.replaceGameData(joinedGame, newData);
                    joinedGame = gameDAO.getGame(reqObj.gameID);
                } else if(reqObj.playerColor.equals("BLACK")){
                    GameData newData = joinedGame.assignBlack(username);
                    gameDAO.replaceGameData(joinedGame, newData);
                    joinedGame = gameDAO.getGame(reqObj.gameID);
                } else {
                    System.out.println("player color was assigned to be something other than WHITE or BLACK");
                }

                if(joinedGame.whiteUsername().equals("")){
                    GameData newData = joinedGame.assignWhite(null);
                    gameDAO.replaceGameData(joinedGame, newData);
                    joinedGame = gameDAO.getGame(reqObj.gameID);
                }
                if(joinedGame.blackUsername().equals("")){
                    GameData newData = joinedGame.assignBlack(null);
                    gameDAO.replaceGameData(joinedGame, newData);
                    joinedGame = gameDAO.getGame(reqObj.gameID);
                }



            } else {
                res.type("application/json");
                res.status(401);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "unauthorized"));
            }

            res.type("application/json");
            res.status(200);
            return String.format("{\"message\": %d}", 200);
        });

        Spark.post("/user", (req, res) -> {
            //Register
            UserData registerReq = new UserData("", "", "");
            try {
                registerReq = serializer.fromJson(req.body(), UserData.class);
                if(registerReq.username() == null || registerReq.password() == null){int i = 37 / 0;}
            } catch(Exception e){
                res.type("application/json");
                res.status(400);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "bad request"));
            }
            if(userDAO.userExists(registerReq)){
                res.type("application/json");
                res.status(403);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "already taken"));
            }

            userDAO.createUser(registerReq);
            AuthData authInfo = authDAO.createAuth(registerReq);

            var json = serializer.toJson(authInfo);

            res.type("application/json");
            return json;
        });

        Spark.delete("/db", (req, res) -> {
            try{
                userDAO.removeAll();
                authDAO.removeAll();
                gameDAO.deleteAll();
            } catch(Exception e){

                res.type("application/json");
                res.status(500);
                return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", e.toString()));
            }

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}