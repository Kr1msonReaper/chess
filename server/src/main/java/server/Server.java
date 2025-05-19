package server;

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
            System.out.println("Got a login request!");
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
            System.out.println("Logout request received!");
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
            System.out.println("Got a list games call!");

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
            System.out.println("Got a create game request!");

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
            System.out.println("Got a join game request!");

            String token = req.headers("authorization");

            if(authDAO.getAuth(token) != null){

                JoinGameRequest reqObj = serializer.fromJson(req.body(), JoinGameRequest.class);
                String username = authDAO.getAuth(token).username();
                GameData joinedGame = gameDAO.getGame(reqObj.gameID);

                if(joinedGame == null || reqObj.playerColor == null || !(reqObj.playerColor.equals("BLACK") || reqObj.playerColor.equals("WHITE"))){
                    res.type("application/json");
                    res.status(400);
                    return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "bad request"));
                }

                if(reqObj.playerColor.equals("WHITE")){
                    if(!joinedGame.whiteUsername().equals("")){
                        res.type("application/json");
                        res.status(403);
                        return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "already taken"));
                    }
                }

                if(reqObj.playerColor.equals("BLACK")){
                    if(!joinedGame.blackUsername().equals("")){
                        res.type("application/json");
                        res.status(403);
                        return String.format("{\"message\": \"%s\"}", String.format("Error: (%s)", "already taken"));
                    }
                }

                if(reqObj.playerColor == "WHITE"){
                    joinedGame.assignWhite(username);
                } else {
                    joinedGame.assignBlack(username);
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
            System.out.println("Got a register request!");
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

            System.out.println("Called delete db!");

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
