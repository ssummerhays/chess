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
    } else {
      newRow -= 1;
    }

    ChessPosition endPosition = new ChessPosition(newRow, newCol);
    ChessMove move = new ChessMove(myPosition, endPosition, null);

    moves.add(move);

    return moves;
  }
}
