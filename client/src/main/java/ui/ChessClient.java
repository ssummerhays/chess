package ui;

import chess.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import model.GameData;
import model.UserData;
import serverfacade.ServerFacade;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.lang.reflect.Type;
import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
  private final ServerFacade serverFacade;
  private final String serverURL;
  public int currentGameID;
  public ChessGame.TeamColor currentColor;
  public State state = State.LOGGED_OUT;
  private String authToken = null;
  private GameData[] gameListArray;
  private WebSocketFacade ws;
  private final NotificationHandler notificationHandler;
  public Printer printer = new Printer();

  public ChessClient(String serverURL, NotificationHandler notificationHandler) {
    serverFacade = new ServerFacade(serverURL);
    this.serverURL = serverURL;
    this.notificationHandler = notificationHandler;
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
        case "leave" -> leaveGame();
        case "highlight" -> highlightMoves(params);
        case "redraw" -> redrawBoard();
        case "move" -> makeMove(params);
        case "resign" -> resign();
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
    } else if (state == State.IN_GAME_PlAYER || state == State.IN_GAME_OBSERVER) {
      return "redraw " + SET_TEXT_COLOR_MAGENTA + "- the chessboard\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "leave " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- the game\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "highlight " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- legal moves\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "move <ChessMove> " + EscapeSequences.SET_TEXT_COLOR_MAGENTA +
              "- make a move <starting_square ending_square promotion_piece?>\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "resign " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- a game\n" +
              EscapeSequences.SET_TEXT_COLOR_BLUE + "help " + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "- with possible commands\n";
    }
    return "create <NAME> " + SET_TEXT_COLOR_MAGENTA + "- a game\n" +
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
      JsonObject result = serverFacade.login(username, password);
      authToken = result.get("authToken").getAsString();
      state = State.LOGGED_IN;
      return String.format("You logged in as %s.\nType help to see more commands", result.get("username").getAsString());
    }
    throw new ResponseException(400, "Expected: <username> <password>");
  }

  public String register(String... params) throws ResponseException {
    if (params.length == 3) {
      var username = params[0];
      var password = params[1];
      var email = params[2];
      UserData userData = new UserData(username, password, email);
      JsonObject res = serverFacade.register(userData);
      state = State.LOGGED_IN;
      authToken = res.get("authToken").getAsString();
      return String.format("Successful Registration. You are now logged in as %s.\nType help to see more commands",
              res.get("username").getAsString());
    }
    throw new ResponseException(400, "Expected: <username> <password> <email>");
  }

  public String logOut() throws ResponseException {
    if (authToken != null) {
      serverFacade.logout(authToken);
      state = State.LOGGED_OUT;
      authToken = null;
      return "Successfully logged out. Type help for more commands";
    }
    throw new ResponseException(400, "Error: already logged out");
  }

  public String createGame(String... params) throws ResponseException {
    assertLoggedIn();
    assertNotInGame();
    if (params.length == 1) {
      String gameName = params[0];
      JsonObject res = serverFacade.createGame(authToken, gameName);
      updateGameList();
      return String.format("Successfully created game: %s%s%s", SET_TEXT_COLOR_MAGENTA, gameName, SET_TEXT_COLOR_BLUE);
    }
    throw new ResponseException(400, "Expected: <gameName>");
  }

  public String listGames() throws ResponseException {
    assertLoggedIn();
    assertNotInGame();
    updateGameList();

    if (gameListArray.length == 0) {
      return SET_TEXT_COLOR_RED + "No active games right now." + SET_TEXT_COLOR_BLUE;
    }
    String result = "";
    for (int i = 1; i <= gameListArray.length; i++) {
      GameData game = gameListArray[i - 1];
      if (game.over() == 0) {
        result+=SET_TEXT_ITALIC + i + RESET_TEXT_ITALIC + ". " + game.gameName() + ": ";
        if (game.whiteUsername() != null && game.blackUsername() != null) {
          result+=SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (white) vs " +
                  SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.blackUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (black)\n";
        } else if (game.whiteUsername() != null) {
          result+=SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE +
                  " (white) vs " + SET_TEXT_COLOR_RED + "(black empty)\n" + SET_TEXT_COLOR_BLUE;
        } else if (game.blackUsername() != null) {
          result+=SET_TEXT_COLOR_RED + "(white empty)" + SET_TEXT_COLOR_BLUE + " vs " + SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA +
                  game.blackUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (black)\n";
        } else {
          result+=SET_TEXT_COLOR_RED + "no players in game currently\n" + SET_TEXT_COLOR_BLUE;
        }
      } else {
        result += i + ". " + game.gameName() + ": " + SET_TEXT_COLOR_YELLOW + "This game has been completed\n" + SET_TEXT_COLOR_BLUE;
      }
    }
    return result;
  }

  public String joinGamePlayer(String... params) throws ResponseException {
    assertLoggedIn();
    assertNotInGame();
    if (gameListArray == null) {
      throw new ResponseException(400, "Error: please list games before trying to join");
    }
    if (params.length == 2) {
      if (!params[0].matches("\\d+")) {
        throw new ResponseException(400, "GameID must be an integer");
      }
      int listNum = Integer.parseInt(params[0]);
      var teamColorStr = params[1];

      if (listNum <= 0) {
        throw new ResponseException(400, "GameID must be greater than 0");
      } else if (listNum > gameListArray.length) {
        throw new ResponseException(400, "No game with this id exists");
      }
      updateGameList();
      GameData gameData = gameListArray[listNum - 1];
      assertGameNotOver(gameData);
      int gameID = gameData.gameID();

      if (!Objects.equals(teamColorStr, "white") && !teamColorStr.equals("black")) {
        throw new ResponseException(400, "Expected [WHITE|BLACK]");
      }
      ChessGame.TeamColor teamColor = (Objects.equals(teamColorStr, "white")) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

      serverFacade.joinGame(authToken, teamColorStr, gameID);
      try {
        ws = new WebSocketFacade(serverURL, notificationHandler, this);
        ws.connectToGame(authToken, gameID, teamColor);
      } catch (Exception e) {
        throw new ResponseException(400, "Error connecting to WebSocket");
      }

      state = State.IN_GAME_PlAYER;
      currentGameID = gameID;
      currentColor = teamColor;

      updateGameList();
      ChessGame game = gameData.game();
      return "";
    }
    throw new ResponseException(400, "Expected: <gameID> [WHITE|BLACK]");
  }

  public String observeGame(String... params) throws ResponseException {
    assertLoggedIn();
    assertNotInGame();
    if (gameListArray == null) {
      throw new ResponseException(400, "Error: please list games before trying to observe");
    }
    if (params.length == 1) {
      if (!params[0].matches("\\d+")) {
        throw new ResponseException(400, "GameID must be an integer");
      }
      int gameID = Integer.parseInt(params[0]);
      if (gameID <= 0) {
        throw new ResponseException(400, "GameID must be greater than 0");
      } else if (gameID > gameListArray.length) {
        throw new ResponseException(400, "No game with this id exists");
      }
      updateGameList();
      GameData gameData = gameListArray[gameID - 1];
      assertGameNotOver(gameData);
      try {
        ws = new WebSocketFacade(serverURL, notificationHandler, this);
        ws.connectToGame(authToken, gameID, null);
      } catch (Exception e) {
        throw new ResponseException(400, "Error connecting to WebSocket");
      }
      state = State.IN_GAME_OBSERVER;
      currentGameID = gameID;
      updateGameList();
      ChessGame game = gameData.game();
      return "";
    }
    throw new ResponseException(400, "Expected: <gameID>");
  }

  public String leaveGame() throws ResponseException {
    assertLoggedIn();
    assertInGame();
    try {
      ws = new WebSocketFacade(serverURL, notificationHandler, this);
      ws.leaveGame(authToken, currentGameID, currentColor);
    } catch (Exception e) {
      throw new ResponseException(400, "Error connecting to WebSocket");
    }
    state = State.LOGGED_IN;
    return "Successfully left game. Type help for more commands";
  }

  public String makeMove(String... params) throws ResponseException {
    assertLoggedIn();
    assertPlayer();
    updateGameList();
    GameData gameData = gameListArray[currentGameID - 1];
    assertGameNotOver(gameData);
    if (params.length == 2 || params.length == 3) {
      if (params[0].length() != 2) {
        throw new ResponseException(400, "Error: starting position should be one letter followed by one number");
      }
      String start = params[0];
      char startLetter = start.charAt(0);
      int startCol;
      switch (startLetter) {
        case 'h' -> startCol = 8;
        case 'g' -> startCol = 7;
        case 'f' -> startCol = 6;
        case 'e' -> startCol = 5;
        case 'd' -> startCol = 4;
        case 'c' -> startCol = 3;
        case 'b' -> startCol = 2;
        case 'a' -> startCol = 1;
        default -> startCol = -1;
      }
      int startRow = Character.getNumericValue(start.charAt(1));
      if (startCol == -1 || startRow < 1 || startRow > 8) {
        throw new ResponseException(400,"Error: invalid starting position");
      }
      ChessPosition startingPosition = new ChessPosition(startRow, startCol);

      if (params[1].length() != 2) {
        throw new ResponseException(400, "Error: ending position should be one letter followed by one number");
      }
      String end = params[1];
      char endLetter = end.charAt(0);
      int endCol;
      switch (endLetter) {
        case 'h' -> endCol = 8;
        case 'g' -> endCol = 7;
        case 'f' -> endCol = 6;
        case 'e' -> endCol = 5;
        case 'd' -> endCol = 4;
        case 'c' -> endCol = 3;
        case 'b' -> endCol = 2;
        case 'a' -> endCol = 1;
        default -> endCol = -1;
      }
      int endRow = Character.getNumericValue(end.charAt(1));
      if (endCol == -1 || endRow < 1 || endRow > 8) {
        throw new ResponseException(400,"Error: invalid ending position");
      }
      ChessPosition endPosition = new ChessPosition(endRow, endCol);

      ChessMove move;

      if (params.length == 3) {
        String promotionStr = params[2].toLowerCase();
        ChessPiece.PieceType type;
        switch (promotionStr) {
          case "queen" -> type = ChessPiece.PieceType.QUEEN;
          case "rook" -> type = ChessPiece.PieceType.ROOK;
          case "bishop" -> type = ChessPiece.PieceType.BISHOP;
          case "knight" -> type = ChessPiece.PieceType.KNIGHT;
          default -> type = null;
        }
        if (type == null) {
          throw new ResponseException(400, "Error: invalid promotion piece");
        }
        move = new ChessMove(startingPosition, endPosition, type);
      } else {
        move = new ChessMove(startingPosition, endPosition, null);
      }

      ChessGame game = gameData.game();
      ChessBoard board = game.getBoard();
      ChessPiece piece = board.getPiece(startingPosition);
      if (piece == null) {
        throw new ResponseException(400, "Error: Starting position has no piece");
      }
      try {
        game.makeMove(move);
      } catch (InvalidMoveException e) {
        throw new ResponseException(400, "Error: out of turn or invalid move");
      }

      GameData data = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game, gameData.over());
      String username = (currentColor == ChessGame.TeamColor.WHITE)? data.whiteUsername() : data.blackUsername();
      try {
        ws = new WebSocketFacade(serverURL, notificationHandler, this);
        ws.makeMove(authToken, currentGameID, move, username);
      } catch (Exception e) {
        throw new ResponseException(400, "Error connecting to WebSocket");
      }
      updateGameList();
      return "";
    }
    throw new ResponseException(400, "Error: expected <startingPosition endingPosition promotionPiece?>");
  }

  public String highlightMoves(String... params) throws ResponseException {
    assertLoggedIn();
    assertInGame();
    if (params.length == 1) {
      String positionStr = params[0];
      if (positionStr.length() == 2) {
        char letter = positionStr.charAt(0);
        char number = positionStr.charAt(1);
        if (!Character.isLetter(letter) || !Character.isDigit(number)) {
          throw new ResponseException(400, "Error: position should be one letter followed by one number");
        }
        updateGameList();
        GameData gameData = gameListArray[currentGameID - 1];
        return printer.printHighlighted(gameData, currentColor, letter, number);
      } else {
        throw new ResponseException(400, "Error: position should be one letter followed by one number");
      }
    } else {
      throw new ResponseException(400, "Expected: <Position>");
    }
  }

  public String redrawBoard() throws ResponseException {
    assertLoggedIn();
    assertInGame();
    updateGameList();
    GameData gameData = gameListArray[currentGameID - 1];
    ChessGame game = gameData.game();
    return printer.printGame(game, currentColor);
  }

  public String resign() throws ResponseException {
    assertLoggedIn();
    assertPlayer();
    Scanner scanner = new Scanner(System.in);
    String warning = "\n" + SET_TEXT_COLOR_YELLOW + "WARNING:" + RESET_TEXT_COLOR + " you have typed resign. " +
            "If you want confirm the resignation type 'y'. If you want to cancel the resignation type 'n' >>> " + SET_TEXT_COLOR_BLUE;
    System.out.print(warning);
    String result = scanner.nextLine();
    while (!Objects.equals(result, "y") && !Objects.equals(result, "n")) {
      System.out.print("Invalid response: please respond with 'y' to resign or 'n' to cancel >>> ");
      result = scanner.nextLine();
    }
    if (result.equals("y")) {
      try {
        ws=new WebSocketFacade(serverURL, notificationHandler, this);
        ws.resign(authToken, currentGameID, currentColor);
      } catch (Exception e) {
        throw new ResponseException(400, "Error connecting to WebSocket");
      }
      updateGameList();
      if (gameListArray[currentGameID - 1].over() == 1) {
        return "Successfully resigned. Type leave to exit";
      } else {
        return "Canceled resignation";
      }
    } else {
      return "Canceled resignation";
    }
  }

  private void assertLoggedIn() throws ResponseException {
    if (state == State.LOGGED_OUT) {
      throw new ResponseException(400, "Error: you must sign in");
    }
  }

  private void assertPlayer() throws ResponseException {
    if (state != State.IN_GAME_PlAYER) {
      throw new ResponseException(400, "Error: you are not currently in game as a player");
    }
  }

  private void assertInGame() throws ResponseException {
    if (state != State.IN_GAME_PlAYER && state != State.IN_GAME_OBSERVER) {
      throw new ResponseException(400, "Error: you are not currently in a game");
    }
  }

  private void assertNotInGame() throws ResponseException {
    if (state == State.IN_GAME_PlAYER || state == State.IN_GAME_OBSERVER) {
      throw new ResponseException(400, "Error: command not available while in game");
    }
  }

  private void assertGameNotOver(GameData gameData) throws ResponseException {
    if (gameData.over() == 1) {
      throw new ResponseException(400, "Error: this game has completed");
    }
  }

  private void updateGameList() throws ResponseException {
    JsonObject res = serverFacade.listGames(authToken);
    Type collectionType = new TypeToken<Collection<GameData>>(){}.getType();

    Collection<GameData> gameList = new Gson().fromJson(res.get("games"), collectionType);

    gameListArray = gameList.toArray(new GameData[0]);
    Arrays.sort(gameListArray, Comparator.comparingInt(GameData::gameID));
  }
}
