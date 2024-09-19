package chess;

import java.util.Collection;
import java.util.HashSet;

public class RookMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    Collection<ChessMove> moves = new HashSet<>();
    int currentRow = myPosition.getRow();
    int currentCol = myPosition.getColumn();

    while (currentRow <= 8) {
      if (currentRow == myPosition.getRow()) {
        currentRow += 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      moves.add(move);
      currentRow += 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();

    while (currentRow > 0) {
      if (currentRow == myPosition.getRow()) {
        currentRow -= 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      moves.add(move);
      currentRow -= 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();

    while (currentCol <= 8) {
      if (currentCol == myPosition.getColumn()) {
        currentCol += 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      moves.add(move);
      currentCol += 1;
    }

    currentRow = myPosition.getRow();
    currentCol = myPosition.getColumn();

    while (currentCol > 0) {
      if (currentCol == myPosition.getColumn()) {
        currentCol -= 1;
        continue;
      }
      ChessPosition endPosition = new ChessPosition(currentRow, currentCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);
      moves.add(move);
      currentCol -= 1;
    }

    return moves;
  }
}
