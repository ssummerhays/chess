package serverfacade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.UserData;

import ui.ResponseException;

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

  public JsonObject register(UserData user) throws ResponseException {
    String path = "/user";
    return this.makeRequest("POST", path, user, JsonObject.class);
  }

  public JsonObject login(String username, String password) throws ResponseException {
    String path = "/session";
    JsonObject data = new JsonObject();
    data.addProperty("username", username);
    data.addProperty("password", password);
    return this.makeRequest("POST", path, data, JsonObject.class);
  }

  public void logout(String authToken) throws ResponseException {
    String path = "/session";
    JsonObject data = new JsonObject();
    data.addProperty("authToken", authToken);
    this.makeRequest("DELETE", path, data, null);
  }

  public JsonObject listGames(String authToken) throws ResponseException{
    String path = "/game";
    JsonObject data = new JsonObject();
    data.addProperty("authToken", authToken);
    return this.makeRequest("GET", path, data, JsonObject.class);
  }

  public JsonObject createGame(String authToken, String gameName) throws  ResponseException {
    String path = "/game";
    JsonObject data = new JsonObject();
    data.addProperty("authToken", authToken);
    data.addProperty("gameName", gameName);
    return this.makeRequest("POST", path, data, JsonObject.class);
  }

  public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
    String path = "/game";
    JsonObject data = new JsonObject();
    if (playerColor != null) {
      playerColor=playerColor.toUpperCase();
    }
    data.addProperty("authToken", authToken);
    data.addProperty("playerColor", playerColor);
    data.addProperty("gameID", gameID);
    this.makeRequest("PUT", path, data, null);
  }

  public void leaveGamePlayer(String authToken, String playerColor, int gameID) throws ResponseException {
    String path = "/game";
    JsonObject data = new JsonObject();
    if (playerColor != null) {
      playerColor = playerColor.toUpperCase();
    }
    data.addProperty("authToken", authToken);
    data.addProperty("playerColor", playerColor);
    data.addProperty("gameID", gameID);
    this.makeRequest("DELETE", path, data, null);
  }

  public void clear() throws ResponseException {
    String path = "/db";
    this.makeRequest("DELETE", path, null, null);
  }

  private <T> T makeRequest(String method, String path, Object data, Class<T> responseClass) throws ResponseException {
    try {
      URL url=(new URI(serverURL + path)).toURL();
      HttpURLConnection http=(HttpURLConnection) url.openConnection();
      http.setRequestMethod(method);
      if (!(Objects.equals(method, "GET"))) {
        http.setDoOutput(true);
      }

      writeBody(data, http);

      http.connect();
      throwIfNotSuccessful(http);
      return readBody(http, responseClass);
    } catch (Exception e) {
      throw new ResponseException(statusCode, e.getMessage());
    }
  }

  private static void writeBody(Object data, HttpURLConnection http) throws IOException {
    if (data != null) {
      http.addRequestProperty("Content-Type", "application/json");
      String reqData = new Gson().toJson(data);
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
