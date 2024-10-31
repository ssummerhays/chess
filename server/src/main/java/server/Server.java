package server;

import dataaccess.*;
import handler.GameHandler;
import handler.UserHandler;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    UserService userService;
    GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;

    public Server(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
    }

    public Server() {
        try {
            UserDataAccess userDataAccess=new MySqlUserDataAccess();
            AuthDataAccess authDataAccess=new MySqlAuthDataAccess();
            GameDataAccess gameDataAccess=new MySqlGameDataAccess();

            this.userService=new UserService(userDataAccess, authDataAccess, gameDataAccess);
            this.gameService=new GameService(authDataAccess, gameDataAccess);

            this.userHandler=new UserHandler(userService);
            this.gameHandler=new GameHandler(gameService);
        } catch (Throwable e) {

        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.delete("/db", userHandler::clear);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
