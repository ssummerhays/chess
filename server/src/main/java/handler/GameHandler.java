package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import service.GameService;
import service.requests.*;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import spark.Request;
import spark.Response;

import java.util.Objects;

public class GameHandler {
  GameService service;

  public GameHandler(GameService gameService) {
    this.service = gameService;
  }

  public String handleError(DataAccessException e, Response res) {
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

  public Object listGames(Request req, Response res) {
    try {
      String authToken = req.headers("Authorization");
      ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
      ListGamesResult listGamesResult = service.listGames(listGamesRequest);

      res.type("application/json");
      res.status(200);
      return new Gson().toJson(listGamesResult);
    } catch (DataAccessException e) {
      return handleError(e, res);
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
      return handleError(e, res);
    }
  }

  public Object joinGame(Request req, Response res) {
    try {
      JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

      JsonObject info = setUpLeaveJoin(req, res);
      if (info.has("error")) {
        return info.get("error").getAsString();
      }
      String authToken = info.get("authToken").getAsString();
      String teamColorStr = body.get("playerColor").getAsString();
      ChessGame.TeamColor teamColor = (Objects.equals(teamColorStr, "WHITE"))? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      int gameID = info.get("gameID").getAsInt();

      JoinGameRequest joinGameRequest= new JoinGameRequest(authToken, teamColor, gameID);
      service.joinGame(joinGameRequest);

      res.type("application/json");
      res.status(200);
      return "{}";

    } catch (DataAccessException e) {
      return handleError(e, res);
    }
  }

  public Object leaveGame(Request req, Response res) {

    try {
      JsonObject info = setUpLeaveJoin(req, res);
      if (info.has("error")) {
        return info.get("error").getAsString();
      }
      String authToken = info.get("authToken").getAsString();
      String teamColorStr = info.get("color").getAsString();
      ChessGame.TeamColor teamColor = (Objects.equals(teamColorStr, "WHITE"))? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      int gameID = info.get("gameID").getAsInt();

      LeaveGameRequest leaveGameRequest= new LeaveGameRequest(authToken, teamColor, gameID);
      service.leaveGame(leaveGameRequest);

      res.type("application/json");
      res.status(200);
      return "{}";

    } catch (DataAccessException e) {
      return handleError(e, res);
    }
  }

  private JsonObject setUpLeaveJoin(Request req, Response res) throws DataAccessException {
    JsonObject result = new JsonObject();

    JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
    int gameID;
    String playerColorString;
    try {
      playerColorString = body.get("playerColor").getAsString();
      gameID = body.get("gameID").getAsInt();

    } catch (Exception e) {
      res.status(400);
      res.type("application/json");
      result.addProperty("error", "{\"message\": \"Error: bad request\"}");
      return result;
    }

    ChessGame.TeamColor teamColor;
    if (Objects.equals(playerColorString, "WHITE")) {
      teamColor = ChessGame.TeamColor.WHITE;
    } else if (Objects.equals(playerColorString, "BLACK")) {
      teamColor = ChessGame.TeamColor.BLACK;
    } else {
      res.status(400);
      res.type("application/json");
      result.addProperty("error", "{\"message\": \"Error: bad request\"}");
      return result;
    }
    String authToken = req.headers("Authorization");

    result.addProperty("authToken", authToken);
    result.addProperty("color", playerColorString);
    result.addProperty("gameID", gameID);
    return result;
  }

  public Object updateGame(Request req, Response res) {
    try {
      JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);
      String jsonGameData;
      try {
        jsonGameData = body.get("gameData").getAsString();
      } catch (Exception e) {
        res.status(400);
        res.type("application/json");
        return "{\"message\": \"Error: bad request\"}";
      }

      String authToken = req.headers("Authorization");

      UpdateGameRequest updateGameRequest= new UpdateGameRequest(authToken, jsonGameData);
      service.updateGame(updateGameRequest);

      res.type("application/json");
      res.status(200);
      return "{}";

    } catch (DataAccessException e) {
      return handleError(e, res);
    }
  }
}
