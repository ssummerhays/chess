package websocket.messages;

public class Error extends ServerMessage {
  String errorMessage;
  public Error(String errorMessage) {
    super(ServerMessageType.ERROR);
    this.errorMessage = errorMessage;
  }

  public String getMessage() {
    return errorMessage;
  }
}
