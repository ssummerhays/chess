package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import dataaccess.MemoryUserDataAccess;
import service.UserService;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;
import spark.Request;
import spark.Response;

public class UserHandler {
  UserService service;

  public UserHandler(MemoryUserDataAccess userDAO, MemoryAuthDataAccess authDAO, MemoryGameDataAccess gameDAO) {
    this.service = new UserService(userDAO, authDAO, gameDAO);
  }
  public Object register(Request req, Response res) {
    try {
      RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);
      RegisterResult registerResult = service.register(registerRequest);

      res.type("application/json");
      res.status(200);
      return new Gson().toJson(registerResult);
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

  public Object login(Request req, Response res) {
    try {
      LoginRequest loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
      LoginResult loginResult = service.login(loginRequest);

      res.type("application/json");
      res.status(200);
      return new Gson().toJson(loginResult);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else {
        res.status(500);
      }
      res.type("application/json");
      return "{\"message\": \"" + e.getMessage() + "\"}";
    } catch (Exception e) {
      res.status(400);
      res.type("application/json");
      return "{\"message\": \"Error: bad request\"}";
    }
  }

  public Object logout(Request req, Response res) {
    try {
      LogoutRequest logoutRequest = new LogoutRequest(req.headers("Authorization"));
      service.logout(logoutRequest);

      res.type("application/json");
      res.status(200);
      return "{}";
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

  public Object clear(Request req, Response res) {
    service.clear();
    res.type("application/json");
    res.status(200);
    return "{}";
  }
}
