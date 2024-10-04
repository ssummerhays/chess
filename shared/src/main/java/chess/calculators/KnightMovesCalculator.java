package chess.calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class KnightMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    Collection<ChessMove> moves=new HashSet<>();

    int currentRow=myPosition.getRow();
    int currentCol=myPosition.getColumn();

    int endRowAbove = currentRow + 2;
    int endRowBelow = currentRow - 2;

    int endColRight = currentCol + 1;
    int endColLeft = currentCol - 1;

    int i = 0;
    while (i < 2) {
      if (endRowAbove <= 8) {

        if (endColRight <= 8) {
          ChessPosition endPosition = new ChessPosition(endRowAbove, endColRight);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              moves.add(move);
            }
          } else {moves.add(move);}
        }

        if (endColLeft > 0) {
          ChessPosition endPosition = new ChessPosition(endRowAbove, endColLeft);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              moves.add(move);
            }
          } else {moves.add(move);}
        }
      }

      if (endRowBelow > 0) {

        if (endColRight <= 8) {
          ChessPosition endPosition = new ChessPosition(endRowBelow, endColRight);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              moves.add(move);
            }
          } else {moves.add(move);}
        }

        if (endColLeft > 0) {
          ChessPosition endPosition = new ChessPosition(endRowBelow, endColLeft);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              moves.add(move);
            }
          } else {moves.add(move);}
        }
      }

      endRowAbove = currentRow + 1;
      endRowBelow = currentRow - 1;

      endColRight = currentCol + 2;
      endColLeft = currentCol - 2;

      i += 1;
    }

    return moves;
  }

  public Collection<ChessMove> pieceMovesCheck(ChessGame game, ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor teamColor) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    Collection<ChessMove> moves=new HashSet<>();

    int currentRow=myPosition.getRow();
    int currentCol=myPosition.getColumn();

    int endRowAbove = currentRow + 2;
    int endRowBelow = currentRow - 2;

    int endColRight = currentCol + 1;
    int endColLeft = currentCol - 1;

    int i = 0;
    while (i < 2) {
      if (endRowAbove <= 8) {

        if (endColRight <= 8) {
          ChessPosition endPosition = new ChessPosition(endRowAbove, endColRight);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          } else {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }

        if (endColLeft > 0) {
          ChessPosition endPosition = new ChessPosition(endRowAbove, endColLeft);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          } else {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }
      }

      if (endRowBelow > 0) {

        if (endColRight <= 8) {
          ChessPosition endPosition = new ChessPosition(endRowBelow, endColRight);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          } else {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }

        if (endColLeft > 0) {
          ChessPosition endPosition = new ChessPosition(endRowBelow, endColLeft);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) != null) {
            if (board.getPiece(endPosition).getTeamColor() != pieceColor){
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          } else {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }
      }

      endRowAbove = currentRow + 1;
      endRowBelow = currentRow - 1;

      endColRight = currentCol + 2;
      endColLeft = currentCol - 2;

      i += 1;
    }

    return moves;
  }

}
