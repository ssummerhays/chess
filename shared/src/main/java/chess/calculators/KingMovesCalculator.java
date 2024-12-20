package chess.calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class KingMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece=board.getPiece(myPosition);
    Collection<ChessMove> moves=new HashSet<>();
    int currentRow=myPosition.getRow();
    int currentCol=myPosition.getColumn();

    for (int r=currentRow - 1; r <= currentRow + 1; r++) {
      if (r>8 || r<=1) {continue;}
      for (int c=currentCol - 1; c <= currentCol + 1; c++) {
        if (c>8 || c<=1) {continue;}
        if (r != currentRow || c != currentCol) {
          ChessPosition endPosition=new ChessPosition(r, c);
          ChessMove move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) == null || board.getPiece(endPosition).getTeamColor() != piece.getTeamColor()) {
            moves.add(move);
          }
        }
      }
    }
    return moves;
  }

}
