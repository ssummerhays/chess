package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import ui.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
  Session session;
  NotificationHandler notificationHandler;

  public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
    try {
      url = url.replace("http", "ws");
      URI socketURI = new URI(url + "/ws");
      this.notificationHandler = notificationHandler;

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      this.session = container.connectToServer(this, socketURI);

      //set message handler
      this.session.addMessageHandler(new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
          Notification notification = new Gson().fromJson(message, Notification.class);
          notificationHandler.notify(notification);
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
      var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, color);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void leaveGame(String authToken, int gameID, ChessGame.TeamColor color) throws ResponseException {
    try {
      var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, color);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }
}
