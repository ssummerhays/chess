package chess;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    Collection<ChessMove> moves=new HashSet<>();

    int newRow = myPosition.getRow();
    int newCol = myPosition.getColumn();

    if (pieceColor == ChessGame.TeamColor.WHITE) {
      newRow += 1;

      ChessPosition endPosition = new ChessPosition(newRow, newCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);

      boolean rightWall = false;
      boolean leftWall = false;

      if (newCol == 8) {
        rightWall = true;
      }
      if (newCol == 1) {
        leftWall = true;
      }

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          }
        }
      }

      if (myPosition.getRow() == 2) {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        newRow += 1;
        endPosition = new ChessPosition(newRow, newCol);
        move = new ChessMove(myPosition, endPosition, null);
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }
      } else if (myPosition.getRow() == 7) {
        endPosition = new ChessPosition(newRow, newCol);

        if (board.getPiece(endPosition) == null) {
          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
          moves.add(move);
        }

        if (newCol < 8) {
          endPosition = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

        if (newCol > 1) {
          endPosition = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

      } else {moves.add(move);}
    }

//    BlackPawn
    else {
      newRow -= 1;

      ChessPosition endPosition = new ChessPosition(newRow, newCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);

      boolean rightWall = false;
      boolean leftWall = false;

      if (newCol == 8) {
        rightWall = true;
      }
      if (newCol == 1) {
        leftWall = true;
      }

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          }
        }
      }

      if (myPosition.getRow() == 7) {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        newRow -= 1;
        endPosition = new ChessPosition(newRow, newCol);
        move = new ChessMove(myPosition, endPosition, null);
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }
      } else if (myPosition.getRow() == 2) {
        endPosition = new ChessPosition(newRow, newCol);

        if (board.getPiece(endPosition) == null) {
          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
          moves.add(move);
        }

        if (newCol < 8) {
          endPosition = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

        if (newCol > 1) {
          endPosition = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }


      } else {moves.add(move);}
    }

    return moves;
  }
}
