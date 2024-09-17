package chess;

import java.util.Collection;
import java.util.HashSet;

public class BishopMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    Collection<ChessMove> moves = new HashSet<>();
    int currentRow = myPosition.getRow();
    int currentCol = myPosition.getColumn();

    while (currentRow <=8 && currentCol <= 8) {
      if (currentRow == myPosition.getRow()) {
        currentRow += 1;
        currentCol += 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      } else {
        if (board.getPiece(endPosition).getTeamColor() != piece.getTeamColor()) {
          moves.add(move);
        }
        break;
      }
      currentRow += 1;
      currentCol += 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();
    while (currentRow > 0 && currentCol > 0) {
      if (currentRow == myPosition.getRow()) {
        currentRow -= 1;
        currentCol -= 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      } else {
        if (board.getPiece(endPosition).getTeamColor() != piece.getTeamColor()) {
          moves.add(move);
        }
        break;
      }
      currentRow -= 1;
      currentCol -= 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();
    while (currentRow <= 8 && currentCol > 0) {
      if (currentRow == myPosition.getRow()) {
        currentRow += 1;
        currentCol -= 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      } else {
        if (board.getPiece(endPosition).getTeamColor() != piece.getTeamColor()) {
          moves.add(move);
        }
        break;
      }
      currentRow += 1;
      currentCol -= 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();
    while (currentRow > 0 && currentCol <= 8) {
      if (currentRow == myPosition.getRow()) {
        currentRow -= 1;
        currentCol += 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      if (board.getPiece(endPosition) == null) {
        moves.add(move);
      } else {
        if (board.getPiece(endPosition).getTeamColor() != piece.getTeamColor()) {
          moves.add(move);
        }
        break;
      }
      currentRow -= 1;
      currentCol += 1;
    }

    return moves;
  }
}
