package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import service.UserService;
import service.requests.LoginRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;
import spark.Request;
import spark.Response;

public class UserHandler {
  private final UserService service = new UserService();
  public Object register(Request req, Response res) {
    try {
      RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);
      RegisterResult registerResult = service.register(registerRequest);

      res.type("application/json");
      res.status(200);
      return new Gson().toJson(registerResult);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("unauthorized")) {
        res.status(401);
      } else if (e.getMessage().contains("already taken")) {
        res.status(403);
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
}
