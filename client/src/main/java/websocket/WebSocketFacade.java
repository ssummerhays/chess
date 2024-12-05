package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import ui.ChessClient;
import ui.EscapeSequences;
import ui.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
  Session session;
  NotificationHandler notificationHandler;
  ChessClient client;

  public WebSocketFacade(String url, NotificationHandler notificationHandler, ChessClient client) throws ResponseException {
    try {
      url = url.replace("http", "ws");
      URI socketURI = new URI(url + "/ws");
      this.notificationHandler = notificationHandler;
      this.client = client;

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      this.session = container.connectToServer(this, socketURI);

      //set message handler
      this.session.addMessageHandler(new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
          ServerMessage messageJson = new Gson().fromJson(message, ServerMessage.class);
          if (messageJson.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            Notification notification = new Gson().fromJson(message, Notification.class);
            notificationHandler.notify(notification);
          }
          else if (messageJson.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
            handleLoadGame(loadGame);
          }
          else if (messageJson.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            Error error = new Gson().fromJson(message, Error.class);
            handleError(error);
          }

        };
      });
    } catch (DeploymentException | IOException | URISyntaxException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {
  }

  public void connectToGame(String authToken, int gameID, ChessGame.TeamColor color) throws ResponseException {
    try {
      var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, color, null, null);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void leaveGame(String authToken, int gameID, ChessGame.TeamColor color) throws ResponseException {
    try {
      var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, color, null, null);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void resign(String authToken, int gameID, ChessGame.TeamColor color) throws ResponseException {
    try {
      var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, color, null, null);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void makeMove(String authToken, int gameID, ChessMove move, String username) throws ResponseException {
    try {
      var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, null, move,
              username);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(400, ex.getMessage());
    }
  }

  public void handleLoadGame(LoadGame loadGame) {
    ChessGame.TeamColor color = (loadGame.getColor() == null)? ChessGame.TeamColor.WHITE : loadGame.getColor();
    String result = client.printGame(loadGame.getGame(), color);
    System.out.print(result);
    System.out.println();
    System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR + "[" + client.state + "] >>> ");
  }

  public void handleError(Error error) {
    System.out.printf("Error: %s", error.getMessage());
    System.out.println();
  }
}
