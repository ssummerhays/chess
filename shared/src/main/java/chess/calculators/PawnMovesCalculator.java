package chess.calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovesCalculator {
  public boolean addBasicMoves(boolean rightWall, boolean leftWall, int newRow, int newCol, ChessBoard board, Collection<ChessMove> moves,
                               ChessPosition myPosition, ChessGame.TeamColor teamColor) {
    boolean ret = false;
    if (rightWall) {
      ChessPosition left = new ChessPosition(newRow, newCol - 1);
      if (board.getPiece(left) == null) {
        ret = true;
      } else {
        if (board.getPiece(left).getTeamColor() != teamColor) {
          ChessMove leftMove = new ChessMove(myPosition, left, null);
          moves.add(leftMove);
        }
      }
    } else if (leftWall) {
      ChessPosition right = new ChessPosition(newRow, newCol + 1);
      if (board.getPiece(right) == null) {
        ret = true;
      } else {
        if (board.getPiece(right).getTeamColor() != teamColor) {
          ChessMove rightMove = new ChessMove(myPosition, right, null);
          moves.add(rightMove);
        }
      }
    } else {
      ChessPosition right = new ChessPosition(newRow, newCol + 1);
      ChessPosition left = new ChessPosition(newRow, newCol - 1);
      if (board.getPiece(right) == null && board.getPiece(left) == null) {
        ret = true;
      } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
        if (board.getPiece(right).getTeamColor() != teamColor) {
          ChessMove rightMove = new ChessMove(myPosition, right, null);
          moves.add(rightMove);
        }
        if (board.getPiece(left).getTeamColor() != teamColor) {
          ChessMove leftMove = new ChessMove(myPosition, left, null);
          moves.add(leftMove);
        }
      } else if (board.getPiece(right) != null) {
        if (board.getPiece(right).getTeamColor() != teamColor) {
          ChessMove rightMove = new ChessMove(myPosition, right, null);
          moves.add(rightMove);
        }
      } else if (board.getPiece(left) != null) {
        if (board.getPiece(left).getTeamColor() != teamColor) {
          ChessMove leftMove = new ChessMove(myPosition, left, null);
          moves.add(leftMove);
        }
      }
    }
    return ret;
  }

  public void addPromotionMoves(int newRow, int newCol, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
    ChessPosition endPosition=new ChessPosition(newRow, newCol);

    ChessMove move;
    if (board.getPiece(endPosition) == null) {
      move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
      moves.add(move);

      move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
      moves.add(move);

      move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
      moves.add(move);

      move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
      moves.add(move);
    }

    if (newCol < 8) {
      endPosition= new ChessPosition(newRow, newCol + 1);
      if (board.getPiece(endPosition) != null) {
        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
        moves.add(move);
      }
    }

    if (newCol > 1) {
      endPosition= new ChessPosition(newRow, newCol - 1);
      if (board.getPiece(endPosition) != null) {
        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
        moves.add(move);

        move= new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
        moves.add(move);
      }
    }
  }

  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();
    Collection<ChessMove> moves=new HashSet<>();
    int newRow = myPosition.getRow();
    int newCol = myPosition.getColumn();
    newRow = (pieceColor == ChessGame.TeamColor.WHITE)? newRow + 1 : newRow - 1;
    ChessPosition endPosition=new ChessPosition(newRow, newCol);
    ChessMove move=new ChessMove(myPosition, endPosition, null);
    boolean rightWall=newCol == 8;
    boolean leftWall=newCol == 1;
    if (board.getPiece(endPosition) != null) {
      if (pieceColor == ChessGame.TeamColor.WHITE) {
        if (addBasicMoves(rightWall, leftWall, newRow, newCol, board, moves, myPosition, ChessGame.TeamColor.WHITE)) {
          return moves;
        }
      } else {
        if (addBasicMoves(rightWall, leftWall, newRow, newCol, board, moves, myPosition, ChessGame.TeamColor.BLACK)) {
          return moves;
        }
      }
    }
    if ((myPosition.getRow() == 2 && pieceColor == ChessGame.TeamColor.WHITE) || (myPosition.getRow() == 7 &&
            pieceColor == ChessGame.TeamColor.BLACK)) {
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      }
      newRow = (pieceColor == ChessGame.TeamColor.WHITE)? newRow + 1 : newRow - 1;
      endPosition=new ChessPosition(newRow, newCol);
      move=new ChessMove(myPosition, endPosition, null);
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      }
      if (pieceColor == ChessGame.TeamColor.WHITE) {
        if (addBasicMoves(rightWall, leftWall, newRow, newCol, board, moves, myPosition, ChessGame.TeamColor.WHITE)) {
          return moves;
        }
      } else {
        if (addBasicMoves(rightWall, leftWall, newRow, newCol, board, moves, myPosition, ChessGame.TeamColor.BLACK)) {
          return moves;
        }
      }
    } else if ((myPosition.getRow() == 7 && pieceColor == ChessGame.TeamColor.WHITE) || (myPosition.getRow() == 2 &&
            pieceColor == ChessGame.TeamColor.BLACK)) {
      addPromotionMoves(newRow, newCol, board, myPosition, moves);
    } else {
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      }
      if (rightWall) {
        ChessPosition left=new ChessPosition(newRow, newCol - 1);
        if (board.getPiece(left) == null) {
          return moves;
        } else {
          if (board.getPiece(left).getTeamColor() != pieceColor) {
            ChessMove leftMove=new ChessMove(myPosition, left, null);
            moves.add(leftMove);
          }
        }
      } else if (leftWall) {
        ChessPosition right=new ChessPosition(newRow, newCol + 1);
        if (board.getPiece(right) == null) {
          return moves;
        } else {
          if (board.getPiece(right).getTeamColor() != pieceColor) {
            ChessMove rightMove=new ChessMove(myPosition, right, null);
            moves.add(rightMove);
          }
        }
      } else {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }
        ChessPosition right=new ChessPosition(newRow, newCol + 1);
        ChessPosition left=new ChessPosition(newRow, newCol - 1);
        if (board.getPiece(right) == null && board.getPiece(left) == null) {
          return moves;
        } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
          if (board.getPiece(right).getTeamColor() != pieceColor) {
            ChessMove rightMove=new ChessMove(myPosition, right, null);
            moves.add(rightMove);
          }
          if (board.getPiece(left).getTeamColor() != pieceColor) {
            ChessMove leftMove=new ChessMove(myPosition, left, null);
            moves.add(leftMove);
          }
        } else if (board.getPiece(right) != null) {
          if (board.getPiece(right).getTeamColor() != pieceColor) {
            ChessMove rightMove=new ChessMove(myPosition, right, null);
            moves.add(rightMove);
          }
        } else if (board.getPiece(left) != null) {
          if (board.getPiece(left).getTeamColor() != pieceColor) {
            ChessMove leftMove=new ChessMove(myPosition, left, null);
            moves.add(leftMove);
          }
        }
      }
    }
    return moves;
  }
}
