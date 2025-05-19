package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import spark.*;
import spark.Spark.*;

public class Server {

    public static MemoryAuthDAO authDAO;
    public static MemoryGameDAO gameDAO;
    public static MemoryUserDAO userDAO;


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        Spark.post("/session", (req, res) -> {

            System.out.println("Got a message!");

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

            System.out.println("Got a message!");

            res.type("application/json");
            return String.format("{\"message\": %d}", 200);
        });

        Spark.delete("/db", (req, res) -> {

            System.out.println("Got a message!");

            try{

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
