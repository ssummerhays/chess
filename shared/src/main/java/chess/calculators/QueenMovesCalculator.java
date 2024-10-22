package chess.calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class QueenMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    Collection<ChessMove> moves = new HashSet<>();

    RookMovesCalculator rookMovesCalculator = new RookMovesCalculator();
    Collection<ChessMove> rookMoves = rookMovesCalculator.pieceMoves(board, myPosition);
    BishopMovesCalculator bishopMovesCalculator = new BishopMovesCalculator();
    Collection<ChessMove> bishopMoves = bishopMovesCalculator.pieceMoves(board, myPosition);

    moves.addAll(rookMoves);
    moves.addAll(bishopMoves);
    return moves;
  }
}
