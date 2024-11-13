package ui;

import server.ServerFacade;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Arrays;

public class ChessClient {
  private final ServerFacade serverFacade;
  private final String serverURL;
  public State state = State.LOGGED_OUT;
  private String authToken = null;

  public ChessClient(String serverURL) {
    serverFacade = new ServerFacade(serverURL);
    this.serverURL = serverURL;
  }

  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var cmd = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);
      return switch (cmd) {
        case "help" -> help();
        case "quit" -> "quit";
        case "login" -> logIn(params);
        case "register" -> register(params);
        case "logout" -> logOut();
        case "create" -> createGame(params);
        case "list" -> listGames();
        case "join" -> joinGamePlayer(params);
        case "observe" -> observeGame(params);
        default -> help();
      };
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  public String help() {
    if (state == State.LOGGED_OUT) {
      return "register <USERNAME> <PASSWORD> <EMAIL> " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- to create an account\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD> " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- to play chess\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "quit " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- playing chess\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "help " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- with possible commands\n";
    }
    return "create <NAME> " + EscapeSequences.RESET_TEXT_COLOR + "- a game\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "list " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + " games\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK] " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- a game\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "observe <ID> " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- a game\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "logout " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- when you are done\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "quit " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- playing chess\n" +
            EscapeSequences.SET_TEXT_COLOR_BLUE + "help " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- with possible commands\n";
  }

  public String logIn(String... params) throws ResponseException {
    if (params.length == 2) {
      var username = params[0];
      var password = params[1];
      LoginRequest req = new LoginRequest(username, password);
      LoginResult result = serverFacade.login(req);
      authToken = result.authToken();
      state = State.LOGGED_IN;
      return String.format("You logged in as %s.", result.username());
    }
    throw new ResponseException(400, "Expected: <username> <password>");
  }

  public String register(String... params) throws ResponseException {
    if (params.length == 3) {
      var username = params[0];
      var password = params[1];
      var email = params[2];
      RegisterRequest req = new RegisterRequest(username, password, email);
      RegisterResult res = serverFacade.register(req);
      state = State.LOGGED_IN;
      authToken = res.authToken();
      return String.format("Successful Registration. You are now logged in as %s.", res.username());
    }
    throw new ResponseException(400, "Expected: <username> <password> <email>");
  }

  public String logOut() throws ResponseException {
    if (!authToken.isEmpty()) {
      LogoutRequest req = new LogoutRequest(authToken);
      serverFacade.logout(req);
      state = State.LOGGED_OUT;
      return "Successfully logged out. Type help for more commands";
    }
    throw new ResponseException(400, "already logged out");
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
