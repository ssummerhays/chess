package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import service.requests.*;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.*;
import java.util.Objects;

public class ServerFacade {
  private final String serverURL;
  private int statusCode = 200;

  public ServerFacade(String url) { serverURL = url; }

  public RegisterResult register(RegisterRequest req) throws Exception {
    String path = "/user";
    return this.makeRequest("POST", path, req, RegisterResult.class);
  }

  public LoginResult login(LoginRequest req) throws Exception {
    String path = "/session";
    return this.makeRequest("POST", path, req, LoginResult.class);
  }

  public void logout(LogoutRequest req) throws Exception {
    String path = "/session";
    this.makeRequest("DELETE", path, req, null);
  }

  public ListGamesResult listGames(ListGamesRequest req) throws Exception{
    String path = "/game";
    return this.makeRequest("GET", path, req, ListGamesResult.class);
  }

  public CreateGameResult createGame(CreateGameRequest req) throws  Exception {
    String path = "/game";
    return this.makeRequest("POST", path, req, CreateGameResult.class);
  }

  public void joinGame(JoinGameRequest req) throws Exception {
    String path = "/game";
    this.makeRequest("PUT", path, req, null);
  }

  public void clear() throws Exception {
    String path = "/db";
    this.makeRequest("DELETE", path, null, null);
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
    try {
      URL url=(new URI(serverURL + path)).toURL();
      HttpURLConnection http=(HttpURLConnection) url.openConnection();
      http.setRequestMethod(method);
      if (!(Objects.equals(method, "GET"))) {
        http.setDoOutput(true);
      }

      writeBody(request, http);

      http.connect();
      throwIfNotSuccessful(http);
      return readBody(http, responseClass);
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

  private static void writeBody(Object request, HttpURLConnection http) throws IOException {
    if (request != null) {
      http.addRequestProperty("Content-Type", "application/json");
      String reqData = new Gson().toJson(request);
      if (reqData.contains("authToken")) {

        JsonObject json = new Gson().fromJson(reqData, JsonObject.class);
        String authToken = json.get("authToken").getAsString();
        http.addRequestProperty("Authorization", authToken);

        json.remove("authToken");
        reqData = new Gson().toJson(json);
      }
      if (!Objects.equals(http.getRequestMethod(), "GET")) {
        try (OutputStream reqBody=http.getOutputStream()) {
          reqBody.write(reqData.getBytes());
        }
      }
    }
  }

  private void throwIfNotSuccessful(HttpURLConnection http) throws Exception {
    var status = http.getResponseCode();
    this.statusCode = status;
    if (!isSuccessful(status)) {
      switch (status) {
        case 400: throw new Exception("Error: bad request");
        case 401: throw new Exception("Error: unauthorized");
        case 403: throw new Exception("Error: already taken");
        case 500: throw new Exception("Error: unknown");
      }
    }
  }

  private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
    T response = null;
    if (http.getContentLength() < 0) {
      try (InputStream respBody = http.getInputStream()) {
        InputStreamReader reader = new InputStreamReader(respBody);
        if (responseClass != null) {
          response = new Gson().fromJson(reader, responseClass);
        }
      }
    }
    return response;
  }

  private boolean isSuccessful(int status) {
    return status / 100 == 2;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
