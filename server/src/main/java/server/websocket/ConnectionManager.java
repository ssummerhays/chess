package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
  public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

  public void add(String username, Session session, int gameID) {
    var connection = new Connection(username, session, gameID);
    connections.put(username, connection);
  }

  public void remove(String username) {
    connections.remove(username);
  }

  public void broadcast(String excludeUsername, int gameID, ServerMessage message) throws IOException {
    var removeList = new ArrayList<Connection>();
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.gameID == gameID && !c.username.equals(excludeUsername)) {
          String jsonMessage=new Gson().toJson(message);
          c.send(jsonMessage);
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.username);
    }
  }

  public void broadcastGame(int gameID, GameData gameData, LoadGame message) throws IOException {
    var removeList = new ArrayList<Connection>();
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.gameID == gameID) {
          ChessGame.TeamColor color = (Objects.equals(gameData.blackUsername(), c.username))? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
          LoadGame newMessage = new LoadGame(message.getGame(), color);
          String jsonMessage=new Gson().toJson(newMessage);
          c.send(jsonMessage);
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.username);
    }
  }

  public void broadCastSelf(String username, ServerMessage message) throws IOException {
    var removeList = new ArrayList<Connection>();
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.username.equals(username)) {
          String jsonMessage = new Gson().toJson(message);
          c.send(jsonMessage);
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.username);
    }
  }

  public void sendError(String username, Error error) throws IOException {
    broadCastSelf(username, error);
  }
}
