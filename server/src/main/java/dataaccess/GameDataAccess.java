package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.PrintedGameData;

import java.util.Collection;

public interface GameDataAccess {
  Collection<PrintedGameData> getGames() throws DataAccessException;

  GameData getGame(int gameID) throws DataAccessException;

  int createGame(String gameName)throws DataAccessException;

  void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException;

  void deleteAllGames() throws DataAccessException;
}
