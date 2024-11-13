package ui;

import server.ServerFacade;

public class ChessClient {
  private final ServerFacade serverFacade;
  private final String serverURL;
  private State state = State.SIGNED_OUT;

  public ChessClient(String serverURL) {
    serverFacade = new ServerFacade(serverURL);
    this.serverURL = serverURL;
  }

  public String eval(String input) {
    return null;
  }

  public String help() {
    return null;
  }

  public String quit() {
    return null;
  }

  public String logIn(String... params) {
    return null;
  }

  public String register(String... params) {
    return null;
  }

  public String logOut() {
    return null;
  }

  public String createGame(String... params) {
    return null;
  }

  public String listGames() {
    return null;
  }

  public String joinGamePlayer(String... params) {
    return null;
  }

  public String observeGame(String... params) {
    return null;
  }
}
