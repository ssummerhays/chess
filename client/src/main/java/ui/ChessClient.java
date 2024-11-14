package ui;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import model.PrintedGameData;
import model.UserData;
import serverfacade.ServerFacade;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static ui.EscapeSequences.*;

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
    if (params.length == 1) {
      String gameName = params[0];
      JsonObject res = serverFacade.createGame(authToken, gameName);
      return String.format("Successfully created game %d. %s", res.get("gameID").getAsInt(), gameName);
    }
    throw new ResponseException(400, "Expected: <gameName>");
  }

  public String listGames() throws ResponseException {
    assertLoggedIn();
    JsonObject res = serverFacade.listGames(authToken);
    Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

    Collection<PrintedGameData> gameList = new Gson().fromJson(res.get("games"), collectionType);

    PrintedGameData[] gameListArray = gameList.toArray(new PrintedGameData[0]);
    Arrays.sort(gameListArray, Comparator.comparingInt(PrintedGameData::gameID));

    if (gameListArray.length == 0) {
      return SET_TEXT_COLOR_RED + "No active games right now." + SET_TEXT_COLOR_BLUE;
    }
    String result = "";
    for (var game : gameListArray) {
      result += SET_TEXT_ITALIC + game.gameID() + RESET_TEXT_ITALIC + ". " + game.gameName() + ": ";
      if (game.whiteUsername() != null && game.blackUsername() != null) {
        result += SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (white) vs " +
                SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.blackUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (black)\n";
      } else if (game.whiteUsername() != null) {
        result += SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE +
                " (white) vs " + SET_TEXT_COLOR_RED + "(black empty)\n" + SET_TEXT_COLOR_BLUE;
      } else if (game.blackUsername() != null) {
        result += SET_TEXT_COLOR_RED + "(white empty)" + SET_TEXT_COLOR_BLUE + " vs " + SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.blackUsername() + RESET_TEXT_BOLD_FAINT +
                SET_TEXT_COLOR_BLUE + " (black)\n";
      } else {
        result += SET_TEXT_COLOR_RED + "no players in game currently\n" + SET_TEXT_COLOR_BLUE;
      }
    }
    return result;
  }

  public String joinGamePlayer(String... params) throws ResponseException {
    assertLoggedIn();
    if (params.length == 2) {
      if (!params[0].matches("\\d+")) {
        throw new ResponseException(400, "GameID must be an integer");
      }
      int gameID = Integer.parseInt(params[0]);
      var teamColorStr = params[1];

      if (!Objects.equals(teamColorStr, "white") && !teamColorStr.equals("black")) {
        throw new ResponseException(400, "Expected [WHITE|BLACK]");
      }
      ChessGame.TeamColor teamColor = (Objects.equals(teamColorStr, "white")) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      ChessGame.TeamColor oppositeColor = (Objects.equals(teamColorStr, "white")) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

      serverFacade.joinGame(authToken, teamColorStr, gameID);

      return printGame(gameID, teamColor) + printGame(gameID, oppositeColor);
    }
    throw new ResponseException(400, "Expected: <gameID> [WHITE|BLACK]");
  }

  public String observeGame(String... params) throws ResponseException {
    assertLoggedIn();
    if (params.length == 1) {
      if (!params[0].matches("\\d+")) {
        throw new ResponseException(400, "GameID must be an integer");
      }
      int gameID = Integer.parseInt(params[0]);
      return printGame(gameID, ChessGame.TeamColor.WHITE) + printGame(gameID, ChessGame.TeamColor.BLACK);
    }
    throw new ResponseException(400, "Expected: <gameID>");
  }

  private void assertLoggedIn() throws ResponseException {
    if (state == State.LOGGED_OUT) {
      throw new ResponseException(400, "you must sign in");
    }
  }

  private String printGame(int gameID, ChessGame.TeamColor color) {
    ChessGame game = new ChessGame();
    String firstLastRow = (color == ChessGame.TeamColor.WHITE)?
            SET_BG_COLOR_LIGHT_GREY + EMPTY + SET_TEXT_COLOR_BLACK + "  a" + EMPTY + " b" + EMPTY + " c" + EMPTY + " d" + EMPTY + " e" +
                    EMPTY + " f" + EMPTY + " g" + EMPTY + " h  " + EMPTY + RESET_BG_COLOR + "\n"
            : SET_BG_COLOR_LIGHT_GREY + EMPTY + SET_TEXT_COLOR_BLACK + "  h" + EMPTY + " g" + EMPTY + " f" + EMPTY + " e" + EMPTY + " d" +
            EMPTY + " c" + EMPTY + " b" + EMPTY + " a  " + EMPTY + RESET_BG_COLOR +"\n";
    String printedBoard = "\n" + firstLastRow;
    for (int r = (color == ChessGame.TeamColor.WHITE)? 7 : 0; (color == ChessGame.TeamColor.WHITE)? r >= 0 : r < 8;
         r = (color == ChessGame.TeamColor.WHITE)? r - 1 : r + 1) {
      int rowNum = r + 1;
      String rowStr = SET_BG_COLOR_LIGHT_GREY + " " + rowNum + " ";
      var row = game.chessBoard.squares[r];
      for (int c = 0; c < 8; c++) {
        ChessPiece piece = row[c];
        ChessPiece.PieceType type = (piece != null)? piece.getPieceType() : null;
        String pieceStr;
        switch (type) {
          case KING -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_KING : BLACK_KING;
          case QUEEN -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_QUEEN : BLACK_QUEEN;
          case BISHOP -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_BISHOP : BLACK_BISHOP;
          case KNIGHT -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_KNIGHT : BLACK_KNIGHT;
          case ROOK -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_ROOK : BLACK_ROOK;
          case PAWN -> pieceStr = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)? WHITE_PAWN : BLACK_PAWN;
          case null -> pieceStr = EMPTY;
        }
        String squareResult;
        String setBgColor = ((rowNum + c) % 2 == 0)? SET_BG_COLOR_MAGENTA : SET_BG_COLOR_WHITE;
        squareResult = setBgColor + " " + pieceStr + " ";
        rowStr += squareResult;
      }
      rowStr += SET_BG_COLOR_LIGHT_GREY + " " + rowNum + " " + RESET_BG_COLOR + "\n";
      printedBoard += rowStr;
    }
    printedBoard += firstLastRow;

    return printedBoard;
  }
}
