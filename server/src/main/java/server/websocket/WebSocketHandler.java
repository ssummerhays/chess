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
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.Objects;

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
      case CONNECT -> connect(authToken, session, command.getGameID());
      case LEAVE -> leave(authToken, session, command.getGameID());
      case RESIGN -> resign(authToken, session, command.getGameID());
    }
  }

  private void connect(String authToken, Session session, int gameID) throws IOException {
    connections.add(authToken, session, gameID);
    String username;
    try {
      AuthData authData=authDAO.getAuth(authToken);
      username=authData.username();
    } catch (DataAccessException e) {
      Error error = new Error("Error: Invalid AuthToken");
      connections.sendError(authToken, error);
      return;
    }
    GameData gameData;
    try {
      gameData=gameDAO.getGame(gameID);
    } catch (DataAccessException e) {
      Error error = new Error("Error: Game Does Not Exist");
      connections.sendError(authToken, error);
      return;
    }

    ChessGame.TeamColor color = null;
    if (Objects.equals(username, gameData.whiteUsername())) {
      color = ChessGame.TeamColor.WHITE;
    } else if (Objects.equals(username, gameData.blackUsername())) {
      color = ChessGame.TeamColor.BLACK;
    }

    String colorStr;
    if (color == null) {
      colorStr = "observer";
    } else {
      colorStr = (color == ChessGame.TeamColor.WHITE)? "white" : "black";
    }


    Notification notification = new Notification("%s has joined the game as %s".formatted(username, colorStr));
    connections.broadcast(authToken, gameID, notification);

    LoadGame loadGame = new LoadGame(gameData.game(), color);
    connections.broadCastSelf(authToken, loadGame);
  }

  private void leave(String authToken, Session session, int gameID) throws IOException {
    String username;
    try {
      AuthData authData=authDAO.getAuth(authToken);
      username=authData.username();
    } catch (DataAccessException e) {
      Error error = new Error("Error: Invalid AuthToken");
      connections.sendError(authToken, error);
      return;
    }
    GameData gameData;
    try {
      gameData=gameDAO.getGame(gameID);
    } catch (DataAccessException e) {
      Error error = new Error("Error: Game Does Not Exist");
      connections.sendError(authToken, error);
      return;
    }

    ChessGame.TeamColor color = null;
    if (Objects.equals(username, gameData.whiteUsername())) {
      color = ChessGame.TeamColor.WHITE;
    } else if (Objects.equals(username, gameData.blackUsername())) {
      color = ChessGame.TeamColor.BLACK;
    }

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
  }

  private void resign(String authToken, Session session, int gameID) throws IOException {
    try {
      String username;
      try {
        AuthData authData=authDAO.getAuth(authToken);
        username=authData.username();
      } catch (DataAccessException e) {
        Error error = new Error("Error: Invalid AuthToken");
        connections.sendError(authToken, error);
        return;
      }
      GameData gameData;
      try {
        gameData=gameDAO.getGame(gameID);
      } catch (DataAccessException e) {
        Error error = new Error("Error: Game Does Not Exist");
        connections.sendError(authToken, error);
        return;
      }

      ChessGame.TeamColor color = null;
      if (Objects.equals(username, gameData.whiteUsername())) {
        color = ChessGame.TeamColor.WHITE;
      } else if (Objects.equals(username, gameData.blackUsername())) {
        color = ChessGame.TeamColor.BLACK;
      }

      if (color == null) {
        Error error = new Error("Error: Observer cannot resign");
        connections.sendError(authToken, error);
      }
      else if (gameData.over() != 0) {
        Error error = new Error("Error: This game is already over");
        connections.sendError(authToken, error);
      }
      else {
        String oppositeUsername=(color == ChessGame.TeamColor.WHITE) ? gameData.blackUsername() : gameData.whiteUsername();
        Notification notification=new Notification("%s resigns. %s has won the game!".formatted(username, oppositeUsername));
        connections.broadcast("", gameID, notification);
        GameData newGameData=new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game(),
                1);
        gameDAO.updateGame(newGameData);
      }
    } catch (DataAccessException e) {
      throw new IOException();
    }
  }
}
