package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import service.GameService;
import service.requests.ListGamesRequest;
import service.results.ListGamesResult;
import spark.Request;
import spark.Response;

public class GameHandler {
  GameService service;

  public GameHandler(MemoryAuthDataAccess authDAO, MemoryGameDataAccess gameDAO) {
    this.service = new GameService(authDAO, gameDAO);
  }

  public Object listGames(Request req, Response res) {
    try {
      String authToken = req.headers("Authorization");
      ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
      ListGamesResult listGamesResult = service.listGames(listGamesRequest);

      res.type("application/json");
      res.status(200);
      return new Gson().toJson(listGamesResult);
    } catch (DataAccessException e) {
      e.printStackTrace();
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else {
        res.status(500);
      }
      res.type("application/json");
      return "{\"message\": \"" + e.getMessage() + "\"}";
    }
  }
}
