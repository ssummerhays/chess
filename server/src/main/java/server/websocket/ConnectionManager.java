package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Error;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
  public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

  public void add(String authToken, Session session, int gameID) {
    var connection = new Connection(authToken, session, gameID);
    connections.put(authToken, connection);
  }

  public void remove(String authToken) {
    connections.remove(authToken);
  }

  public void broadcast(String excludeAuth, int gameID, ServerMessage message) throws IOException {
    var removeList = new ArrayList<Connection>();
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.gameID == gameID) {
          if (!c.authToken.equals(excludeAuth)) {
            String jsonMessage=new Gson().toJson(message);
            c.send(jsonMessage);
          }
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.authToken);
    }
  }

  public void broadCastSelf(String authToken, ServerMessage message) throws IOException {
    var removeList = new ArrayList<Connection>();
    for (var c : connections.values()) {
      if (c.session.isOpen()) {
        if (c.authToken.equals(authToken)) {
          String jsonMessage = new Gson().toJson(message);
          c.send(jsonMessage);
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up any connections that were left open.
    for (var c : removeList) {
      connections.remove(c.authToken);
    }
  }

  public void sendError(String authToken, Error error) throws IOException {
    broadCastSelf(authToken, error);
  }
}
