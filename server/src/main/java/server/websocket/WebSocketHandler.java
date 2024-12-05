package server.websocket;

import chess.*;
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
      case MAKE_MOVE -> makeMove(authToken, session, command.getGameID(), command.getMove(), command.getUsername());
    }
  }

  private void connect(String authToken, Session session, int gameID) throws IOException {
    String username = "badAuth";
    try {
      AuthData authData=authDAO.getAuth(authToken);
      username=authData.username();
      connections.add(username, session, gameID);
    } catch (DataAccessException e) {
      connections.add(username, session, gameID);
      Error error = new Error("Error: Invalid AuthToken");
      connections.sendError(username, error);
      return;
    }
    GameData gameData;
    try {
      gameData=gameDAO.getGame(gameID);
    } catch (DataAccessException e) {
      Error error = new Error("Error: Game Does Not Exist");
      connections.sendError(username, error);
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
    connections.broadcast(username, gameID, notification);

    LoadGame loadGame = new LoadGame(gameData.game(), color);
    connections.broadCastSelf(username, loadGame);
  }

  private void leave(String authToken, Session session, int gameID) throws IOException {
    String username = "badAuth";
    try {
      AuthData authData=authDAO.getAuth(authToken);
      username=authData.username();
    } catch (DataAccessException e) {
      Error error = new Error("Error: Invalid AuthToken");
      connections.sendError(username, error);
      return;
    }
    GameData gameData;
    try {
      gameData=gameDAO.getGame(gameID);
    } catch (DataAccessException e) {
      Error error = new Error("Error: Game Does Not Exist");
      connections.sendError(username, error);
      return;
    }

    ChessGame.TeamColor color = null;
    if (Objects.equals(username, gameData.whiteUsername())) {
      color = ChessGame.TeamColor.WHITE;
    } else if (Objects.equals(username, gameData.blackUsername())) {
      color = ChessGame.TeamColor.BLACK;
    }

    try {
      if (color != null) {
        gameDAO.leaveGame(gameData, username, color);
      }
    } catch (DataAccessException e) {
      throw new IOException();
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
    connections.broadcast(username, gameID, notification);
    connections.remove(username);
  }

  private void resign(String authToken, Session session, int gameID) throws IOException {
    try {
      String username = "badAuth";
      try {
        AuthData authData=authDAO.getAuth(authToken);
        username=authData.username();
      } catch (DataAccessException e) {
        Error error = new Error("Error: Invalid AuthToken");
        connections.sendError(username, error);
        return;
      }
      GameData gameData;
      try {
        gameData=gameDAO.getGame(gameID);
      } catch (DataAccessException e) {
        Error error = new Error("Error: Game Does Not Exist");
        connections.sendError(username, error);
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
        connections.sendError(username, error);
      }
      else if (gameData.over() != 0) {
        Error error = new Error("Error: This game is already over");
        connections.sendError(username, error);
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

  private void makeMove(String authToken, Session session, int gameID, ChessMove move, String username) throws IOException {
    try {
      GameData gameData;
      try {
        gameData=gameDAO.getGame(gameID);
      } catch (DataAccessException e) {
        Error error = new Error("Error: Game Does Not Exist");
        connections.sendError(username, error);
        return;
      }
      ChessPosition startPosition = move.getStartPosition();
      ChessGame game = gameData.game();
      ChessPiece piece = game.getBoard().getPiece(startPosition);

      if (piece != null) {
        username = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? gameData.whiteUsername() : gameData.blackUsername();
      }
      try {
        AuthData authData=authDAO.getAuth(authToken);
        username=authData.username();
      } catch (DataAccessException e) {
        Error error = new Error("Error: Invalid AuthToken");
        connections.sendError(username, error);
        return;
      }

      String opponent = "";
      ChessGame.TeamColor color = null;
      ChessGame.TeamColor oppositeColor = null;
      if (Objects.equals(username, gameData.whiteUsername())) {
        color = ChessGame.TeamColor.WHITE;
        oppositeColor = ChessGame.TeamColor.BLACK;
        opponent = gameData.blackUsername();
      } else if (Objects.equals(username, gameData.blackUsername())) {
        color = ChessGame.TeamColor.BLACK;
        oppositeColor = ChessGame.TeamColor.WHITE;
        opponent = gameData.whiteUsername();
      }

      if (color == null) {
        Error error = new Error("Error: Observer cannot make move");
        connections.sendError(username, error);
        return;
      }
      else if (gameData.over() != 0) {
        Error error = new Error("Error: This game is already over");
        connections.sendError(username, error);
        return;
      }

      if (piece == null) {
        Error error = new Error("Error: No piece at this position");
        connections.sendError(username, error);
      }
      else if (piece.getTeamColor() != color) {
        Error error = new Error("Error: Incorrect piece color");
        connections.sendError(username, error);
      }
      else if (game.getTeamTurn() != color) {
        Error error = new Error("Error: Not your turn");
        connections.sendError(username, error);
      }
      else if (!game.validMoves(startPosition).contains(move)) {
        Error error = new Error("Error: Invalid Move");
        connections.sendError(username, error);
      }
      else {
        String startLetter = "";
        switch(startPosition.getColumn()) {
          case 1 -> startLetter = "a";
          case 2 -> startLetter = "b";
          case 3 -> startLetter = "c";
          case 4 -> startLetter = "d";
          case 5 -> startLetter = "e";
          case 6 -> startLetter = "f";
          case 7 -> startLetter = "g";
          case 8 -> startLetter = "h";
        }
        String start = startLetter + startPosition.getRow();

        String endLetter = "";
        switch(startPosition.getColumn()) {
          case 1 -> endLetter = "a";
          case 2 -> endLetter = "b";
          case 3 -> endLetter = "c";
          case 4 -> endLetter = "d";
          case 5 -> endLetter = "e";
          case 6 -> endLetter = "f";
          case 7 -> endLetter = "g";
          case 8 -> endLetter = "h";
        }
        String end = endLetter + move.getEndPosition().getRow();
        game.makeMove(move);
        gameData = gameDAO.getGame(gameID);

        LoadGame loadGame = new LoadGame(game, color);
        connections.broadcastGame( gameID, gameData, loadGame);

        Notification notification=new Notification("%s makes move: %s -> %s".formatted(username, start, end));
        connections.broadcast(username, gameID, notification);
        Notification gameStateNotification;

        int over = 0;
        if (game.isInCheckmate(oppositeColor)) {
          gameStateNotification = new Notification("%s wins by checkmate!\nThe game is over. Type leave to exit".formatted(username));
          connections.broadcast("", gameID, gameStateNotification);
          over = 1;
        } else if (game.isInStalemate(color)) {
          gameStateNotification = new Notification("Stalemate!\nThe game ends in a draw. Type leave to exit");
          connections.broadcast("", gameID, gameStateNotification);
          over = 1;
        } else if (game.isInCheck(oppositeColor)) {
          gameStateNotification = new Notification("%s is in check!".formatted(opponent));
          connections.broadcast("", gameID, gameStateNotification);
        }
        GameData finalData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game, over);
        gameDAO.updateGame(finalData);

      }
    } catch (Exception e) {
      throw new IOException();
    }
  }
}
