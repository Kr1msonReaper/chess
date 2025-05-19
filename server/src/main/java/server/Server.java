package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
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



            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.delete("/session", (req, res) -> {

            System.out.println("Got a message!");

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.get("/game", (req, res) -> {

            System.out.println("Got a message!");

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.post("/game", (req, res) -> {

            System.out.println("Got a message!");

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.put("/game", (req, res) -> {

            System.out.println("Got a message!");

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.post("/user", (req, res) -> {
            //Register
            System.out.println("Got a register request!");
            UserData registerReq = new UserData("", "", "");
            try {
                registerReq = serializer.fromJson(req.body(), UserData.class);
                if(registerReq.username() == null || registerReq.password() == null || registerReq.email() == null){int i = 37 / 0;}
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
