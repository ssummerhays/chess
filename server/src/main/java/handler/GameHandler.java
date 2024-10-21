package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import service.GameService;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import spark.Request;
import spark.Response;

import java.util.Objects;

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
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else {
        res.status(500);
      }
      res.type("application/json");
      return "{\"message\": \"" + e.getMessage() + "\"}";
    }
  }

  public Object createGame(Request req, Response res) {
    try {
      JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
      String gameName = body.get("gameName").getAsString();
      String authToken = req.headers("Authorization");

      CreateGameRequest createGameRequest=new CreateGameRequest(authToken, gameName);
      CreateGameResult createGameResult=service.createGame(createGameRequest);
      res.type("application/json");
      res.status(200);
      return new Gson().toJson(createGameResult);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else {
        res.status(500);
      }
      res.type("application/json");
      return "{\"message\": \"" + e.getMessage() + "\"}";
    }
  }

  public Object joinGame(Request req, Response res) {
    try {
      JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
      int gameID;
      String playerColorString;
      try {
        playerColorString = body.get("playerColor").getAsString();
        gameID = body.get("gameID").getAsInt();

      } catch (Exception e) {
        res.status(400);
        res.type("application/json");
        return "{\"message\": \"Error: bad request\"}";
      }

      ChessGame.TeamColor teamColor;
      if (Objects.equals(playerColorString, "WHITE")) {
        teamColor = ChessGame.TeamColor.WHITE;
      } else if (Objects.equals(playerColorString, "BLACK")) {
        teamColor = ChessGame.TeamColor.BLACK;
      } else {
        res.status(400);
        res.type("application/json");
        return "{\"message\": \"Error: bad request\"}";
      }
      String authToken = req.headers("Authorization");

      JoinGameRequest joinGameRequest= new JoinGameRequest(authToken, teamColor, gameID);
      service.joinGame(joinGameRequest);

      res.type("application/json");
      res.status(200);
      return "{}";

    } catch (DataAccessException e) {
      if (e.getMessage().contains("bad request")) {
        res.status(400);
      } else if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else if (e.getMessage().contains("already taken")) {
        res.status(403);
      } else {
        res.status(500);
      }
      res.type("application/json");
      return "{\"message\": \"" + e.getMessage() + "\"}";
    }
  }
}
