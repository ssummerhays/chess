package ui;

import model.PrintedGameData;
import server.ServerFacade;
import service.requests.*;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Arrays;
import java.util.Comparator;

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
      return String.format("You logged in as %s.\nType help to see more commands", result.username());
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
      return String.format("Successful Registration. You are now logged in as %s.\nType help to see more commands", res.username());
    }
    throw new ResponseException(400, "Expected: <username> <password> <email>");
  }

  public String logOut() throws ResponseException {
    if (authToken != null) {
      LogoutRequest req = new LogoutRequest(authToken);
      serverFacade.logout(req);
      state = State.LOGGED_OUT;
      authToken = null;
      return "Successfully logged out. Type help for more commands";
    }
    throw new ResponseException(400, "already logged out");
  }

  public String createGame(String... params) throws ResponseException {
    assertLoggedIn();
    if (params.length == 1) {
      String gameName = params[0];
      CreateGameRequest req = new CreateGameRequest(authToken, gameName);
      CreateGameResult res = serverFacade.createGame(req);
      return String.format("Successfully created game %d. %s", res.gameID(), gameName);
    }
    throw new ResponseException(400, "Expected: <gameName>");
  }

  public String listGames() throws ResponseException {
    assertLoggedIn();
    ListGamesRequest req = new ListGamesRequest(authToken);
    ListGamesResult res = serverFacade.listGames(req);
    PrintedGameData[] gameList = res.games().toArray(new PrintedGameData[0]);
    Arrays.sort(gameList, Comparator.comparingInt(PrintedGameData::gameID));

    if (gameList.length == 0) {
      return "No active games right now.";
    }
    String result = "";
    for (var game : gameList) {
      if (game.whiteUsername() != null && game.blackUsername() != null) {
        result += game.gameID() + ". " + game.gameName() + ": " + game.whiteUsername() + "(white) vs " + game.blackUsername() + "(black)\n";
      } else if (game.whiteUsername() != null) {
        result += game.gameID() + ". " + game.gameName() + ": " + game.whiteUsername() + "(white) vs (black empty)\n";
      } else if (game.blackUsername() != null) {
        result += game.gameID() + ". " + game.gameName() + ": (white empty) vs " + game.blackUsername() + "(black)\n";
      } else {
        result += game.gameID() + ". " + game.gameName() + ": no players in game currently\n";
      }
    }
    return result;
  }

  public String joinGamePlayer(String... params) {
    return null;
  }

  public String observeGame(String... params) {
    return null;
  }

  private void assertLoggedIn() throws ResponseException {
    if (state == State.LOGGED_OUT) {
      throw new ResponseException(400, "you must sign in");
    }
  }
}
