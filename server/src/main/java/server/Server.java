package server;

import dataaccess.*;
import handler.GameHandler;
import handler.UserHandler;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import server.websocket.WebSocketHandler;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    UserService userService;
    GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;

    public UserDataAccess userDataAccess;
    public AuthDataAccess authDataAccess;
    public GameDataAccess gameDataAccess;

    private WebSocketHandler webSocketHandler;

    public Server(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
        this.webSocketHandler = new WebSocketHandler();
        webSocketHandler.setDataAccesses(userService.userDAO, userService.authDAO, userService.gameDAO);
    }

    public Server() {
        try {
            boolean mySql = true;
            if (mySql) {
                userDataAccess=new MySqlUserDataAccess();
                authDataAccess=new MySqlAuthDataAccess();
                gameDataAccess=new MySqlGameDataAccess();
            } else {
                userDataAccess = new MemoryUserDataAccess();
                authDataAccess = new MemoryAuthDataAccess();
                gameDataAccess = new MemoryGameDataAccess();
            }

            this.userService=new UserService(userDataAccess, authDataAccess, gameDataAccess);
            this.gameService=new GameService(authDataAccess, gameDataAccess);

            this.userHandler=new UserHandler(userService);
            this.gameHandler=new GameHandler(gameService);

            this.webSocketHandler.setDataAccesses(userDataAccess, authDataAccess, gameDataAccess);
        } catch (Throwable e) {

        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.webSocket("/ws", webSocketHandler);

        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);
        Spark.delete("/db", userHandler::clear);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);
        Spark.delete("/game", gameHandler::leaveGame);
        Spark.post("/move", gameHandler::updateGame);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
