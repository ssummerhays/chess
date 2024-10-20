package server;

import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import dataaccess.MemoryUserDataAccess;
import handler.GameHandler;
import handler.UserHandler;
import spark.*;

public class Server {
    MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    MemoryGameDataAccess gameDAO = new MemoryGameDataAccess();
    private final UserHandler userHandler = new UserHandler(userDAO, authDAO, gameDAO);
    private final GameHandler gameHandler = new GameHandler(authDAO, gameDAO);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.delete("/db", userHandler::clear);
        Spark.get("/game", gameHandler::listGames);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
