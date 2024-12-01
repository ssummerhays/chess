package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import model.GameData;
import model.PrintedGameData;
import model.UserData;
import serverfacade.ServerFacade;

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
  private PrintedGameData[] gameListArray;

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
        case "leave" -> leaveGame();
        case "highlight" -> highlightMoves(params);
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
              "- make a move <starting_square ending_square>\n" +
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
    if (params.length == 1) {
      String gameName = params[0];
      JsonObject res = serverFacade.createGame(authToken, gameName);
      return String.format("Successfully created game: %s%s%s", SET_TEXT_COLOR_MAGENTA, gameName, SET_TEXT_COLOR_BLUE);
    }
    throw new ResponseException(400, "Expected: <gameName>");
  }

  public String listGames() throws ResponseException {
    assertLoggedIn();
    JsonObject res = serverFacade.listGames(authToken);
    Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

    Collection<PrintedGameData> gameList = new Gson().fromJson(res.get("games"), collectionType);

    gameListArray = gameList.toArray(new PrintedGameData[0]);
    Arrays.sort(gameListArray, Comparator.comparingInt(PrintedGameData::gameID));

    if (gameListArray.length == 0) {
      return SET_TEXT_COLOR_RED + "No active games right now." + SET_TEXT_COLOR_BLUE;
    }
    String result = "";
    for (int i = 1; i <= gameList.size(); i++) {
      PrintedGameData game = gameListArray[i - 1];
      result += SET_TEXT_ITALIC + i + RESET_TEXT_ITALIC + ". " + game.gameName() + ": ";
      if (game.whiteUsername() != null && game.blackUsername() != null) {
        result += SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (white) vs " +
                SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.blackUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (black)\n";
      } else if (game.whiteUsername() != null) {
        result += SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA + game.whiteUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE +
                " (white) vs " + SET_TEXT_COLOR_RED + "(black empty)\n" + SET_TEXT_COLOR_BLUE;
      } else if (game.blackUsername() != null) {
        result += SET_TEXT_COLOR_RED + "(white empty)" + SET_TEXT_COLOR_BLUE + " vs " + SET_TEXT_BOLD + SET_TEXT_COLOR_MAGENTA +
                game.blackUsername() + RESET_TEXT_BOLD_FAINT + SET_TEXT_COLOR_BLUE + " (black)\n";
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
      int listNum = Integer.parseInt(params[0]);
      var teamColorStr = params[1];

      if (listNum <= 0) {
        throw new ResponseException(400, "GameID must be greater than 0");
      } else if (listNum > gameListArray.length) {
        throw new ResponseException(400, "No game with this id exists");
      }
      PrintedGameData gameData = gameListArray[listNum - 1];
      int gameID = gameData.gameID();

      if (!Objects.equals(teamColorStr, "white") && !teamColorStr.equals("black")) {
        throw new ResponseException(400, "Expected [WHITE|BLACK]");
      }
      ChessGame.TeamColor teamColor = (Objects.equals(teamColorStr, "white")) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
      ChessGame.TeamColor oppositeColor = (Objects.equals(teamColorStr, "white")) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

      serverFacade.joinGame(authToken, teamColorStr, gameID);

      state = State.IN_GAME_PlAYER;
      currentGameID = gameID;
      currentColor = teamColor;
      return printGame(gameID, teamColor);
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
      if (gameID <= 0) {
        throw new ResponseException(400, "GameID must be greater than 0");
      } else if (gameID > gameListArray.length) {
        throw new ResponseException(400, "No game with this id exists");
      }
      state = State.IN_GAME_OBSERVER;
      return printGame(gameID, ChessGame.TeamColor.WHITE);
    }
    throw new ResponseException(400, "Expected: <gameID>");
  }

  public String leaveGame() throws ResponseException {
    assertLoggedIn();
    assertInGame();
    state = State.LOGGED_IN;
    return "Successfully left game. Type help for more commands";
  }

  public String highlightMoves(String... params) throws ResponseException {
    assertLoggedIn();
    assertPlayer();
    if (params.length == 1) {
      String positionStr = params[0];
      if (positionStr.length() == 2) {
        char letter = positionStr.charAt(0);
        char number = positionStr.charAt(1);
        if (!Character.isLetter(letter) || !Character.isDigit(number)) {
          throw new ResponseException(400, "Error: position should be one letter followed by one number");
        }
        return printHighlighted(currentGameID, currentColor, letter, number);
      } else {
        throw new ResponseException(400, "Error: position should be one letter followed by one number");
      }
    } else {
      throw new ResponseException(400, "Expected: <Position>");
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
      for (int c = (color == ChessGame.TeamColor.BLACK)? 7 : 0; (color == ChessGame.TeamColor.BLACK)? c >= 0 : c < 8;
           c = (color == ChessGame.TeamColor.BLACK)? c - 1 : c + 1) {
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
        String setBgColor= ((rowNum + c) % 2 == 0)? SET_BG_COLOR_WHITE : SET_BG_COLOR_MAGENTA;

        squareResult = setBgColor + " " + pieceStr + " ";
        rowStr += squareResult;
      }
      rowStr += SET_BG_COLOR_LIGHT_GREY + " " + rowNum + " " + RESET_BG_COLOR + "\n";
      printedBoard += rowStr;
    }
    printedBoard += firstLastRow;

    return printedBoard;
  }

  private String printHighlighted(int gameID, ChessGame.TeamColor color, char letter, char number) throws ResponseException {
    ChessGame game = new ChessGame();
    String firstLastRow = (color == ChessGame.TeamColor.WHITE)?
            SET_BG_COLOR_LIGHT_GREY + EMPTY + SET_TEXT_COLOR_BLACK + "  a" + EMPTY + " b" + EMPTY + " c" + EMPTY + " d" + EMPTY + " e" +
                    EMPTY + " f" + EMPTY + " g" + EMPTY + " h  " + EMPTY + RESET_BG_COLOR + "\n"
            : SET_BG_COLOR_LIGHT_GREY + EMPTY + SET_TEXT_COLOR_BLACK + "  h" + EMPTY + " g" + EMPTY + " f" + EMPTY + " e" + EMPTY + " d" +
            EMPTY + " c" + EMPTY + " b" + EMPTY + " a  " + EMPTY + RESET_BG_COLOR +"\n";
    String printedBoard = "\n" + firstLastRow;
    int pieceRow = Character.getNumericValue(number);
    int pieceColumn;
    switch (letter) {
      case 'h' -> pieceColumn = 8;
      case 'g' -> pieceColumn = 7;
      case 'f' -> pieceColumn = 6;
      case 'e' -> pieceColumn = 5;
      case 'd' -> pieceColumn = 4;
      case 'c' -> pieceColumn = 3;
      case 'b' -> pieceColumn = 2;
      case 'a' -> pieceColumn = 1;
      default -> pieceColumn = -1;
    }
    if (pieceRow < 1 || pieceRow > 8 || pieceColumn == -1) {
      throw new ResponseException(400, "Error: invalid position");
    }

    Collection<ChessMove> validMoves = new HashSet<>();
    ChessPosition highlightPosition = new ChessPosition(pieceRow, pieceColumn);
    if (game.chessBoard.squares[pieceRow - 1][pieceColumn - 1] != null) {
      validMoves = game.validMoves(highlightPosition);
    }

    for (int r = (color == ChessGame.TeamColor.WHITE)? 7 : 0; (color == ChessGame.TeamColor.WHITE)? r >= 0 : r < 8;
         r = (color == ChessGame.TeamColor.WHITE)? r - 1 : r + 1) {
      int rowNum = r + 1;
      String rowStr = SET_BG_COLOR_LIGHT_GREY + " " + rowNum + " ";
      var row = game.chessBoard.squares[r];
      for (int c = (color == ChessGame.TeamColor.BLACK)? 7 : 0; (color == ChessGame.TeamColor.BLACK)? c >= 0 : c < 8;
           c = (color == ChessGame.TeamColor.BLACK)? c - 1 : c + 1) {
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

        ChessPosition currentPosition = new ChessPosition(rowNum, c + 1);
        ChessMove currentMove = new ChessMove(highlightPosition, currentPosition, null);
        if (piece != null) {
          if ((r == 7 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) || (r == 0 && piece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
            currentMove = new ChessMove(highlightPosition, currentPosition, ChessPiece.PieceType.QUEEN);
          }
        } else {

        }

        String setBgColor = ((rowNum + c) % 2 == 0)? SET_BG_COLOR_WHITE : SET_BG_COLOR_MAGENTA;
        if (currentPosition.getRow() == highlightPosition.getRow() && currentPosition.getColumn() == highlightPosition.getColumn()) {
          setBgColor = SET_BG_COLOR_YELLOW;
        }
        else if (validMoves.contains(currentMove)) {
          setBgColor = ((rowNum + c) % 2 == 0)? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
        }

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
