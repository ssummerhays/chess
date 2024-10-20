package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import service.UserService;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import spark.Request;
import spark.Response;

public class UserHandler {
  private final UserService service = new UserService();
  public Object register(Request req, Response res) throws DataAccessException {
    RegisterRequest registerRequest = new Gson().fromJson(req.body(), RegisterRequest.class);
    RegisterResult registerResult = service.register(registerRequest);

    res.type("application/json");
    res.status(200);
    return new Gson().toJson(registerResult);
  }
}
