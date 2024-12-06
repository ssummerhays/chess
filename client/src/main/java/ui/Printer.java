package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;

import static ui.EscapeSequences.*;

public class Printer {

  public String printGame(ChessGame game, ChessGame.TeamColor color) {
    color = (color == null)? ChessGame.TeamColor.WHITE : color;
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

  public String printHighlighted(GameData gameData, ChessGame.TeamColor color, char letter, char number) throws ResponseException {
    ChessGame game = gameData.game();
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
        String pieceStr=getString(piece);
        String squareResult;

        ChessPosition currentPosition = new ChessPosition(rowNum, c + 1);
        ChessMove currentMove = new ChessMove(highlightPosition, currentPosition, null);
        if (piece != null) {
          if ((r == 7 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) || (r == 0 && piece.getTeamColor() == ChessGame.TeamColor.BLACK)) {
            currentMove = new ChessMove(highlightPosition, currentPosition, ChessPiece.PieceType.QUEEN);
          }
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

  private static String getString(ChessPiece piece) {
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
    return pieceStr;
  }
}
