package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

  private final ConnectionManager connections = new ConnectionManager();

  UserDataAccess userDAO;
  AuthDataAccess authDAO;
  GameDataAccess gameDAO;

  public void setDataAccesses(UserDataAccess userDAO, AuthDataAccess authDAO, GameDataAccess gameDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {
    UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
    String authToken = command.getAuthToken();

    switch (command.getCommandType()) {
      case CONNECT -> connect(authToken, session, command.getGameID(), command.getColor());
      case LEAVE -> leave(authToken, session, command.getGameID(), command.getColor());
      case RESIGN -> resign(authToken, session, command.getGameID(), command.getColor());
    }
  }

  private void connect(String authToken, Session session, int gameID, ChessGame.TeamColor color) throws IOException {
    try {
      AuthData authData = authDAO.getAuth(authToken);
      String username = authData.username();
      String colorStr;
      if (color == null) {
        colorStr = "observer";
      } else {
        colorStr = (color == ChessGame.TeamColor.WHITE)? "white" : "black";
      }

      connections.add(authToken, session, gameID);
      Notification notification = new Notification("%s has joined the game as %s".formatted(username, colorStr));
      connections.broadcast(authToken, gameID, notification);
    } catch (DataAccessException e) {
      throw new IOException();
    }
  }

  private void leave(String authToken, Session session, int gameID, ChessGame.TeamColor color) throws IOException {
    try {
      AuthData authData = authDAO.getAuth(authToken);
      String username = authData.username();
      String result;
      if (color == null) {
        result = "Observer %s has left the game".formatted(username);
      } else if (color == ChessGame.TeamColor.WHITE) {
        result = "White Player %s has left the game".formatted(username);
      } else {
        result = "Black Player %s has left the game".formatted(username);
      }
      Notification notification = new Notification(result);
      connections.broadcast(authToken, gameID, notification);
      connections.remove(authToken);
    } catch (DataAccessException e) {
      throw new IOException();
    }
  }

  private void resign(String authToken, Session session, int gameID, ChessGame.TeamColor color) throws IOException {
    try {
      AuthData authData = authDAO.getAuth(authToken);
      String username = authData.username();

      GameData gameData = gameDAO.getGame(gameID);
      String oppositeUsername = (color == ChessGame.TeamColor.WHITE)? gameData.blackUsername() : gameData.whiteUsername();
      Notification notification = new Notification("%s resigns. %s has won the game!".formatted(username, oppositeUsername));
      connections.broadcast(authToken, gameID, notification);
      GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game(),
              1);
      gameDAO.updateGame(newGameData);
    } catch (DataAccessException e) {
      throw new IOException();
    }
  }
}
