package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.PrintedGameData;

import java.util.Collection;

public interface GameDataAccess {
  Collection<GameData> getGames() throws DataAccessException;

  GameData getGame(int gameID) throws DataAccessException;

  int createGame(String gameName) throws DataAccessException;

  void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException;

  void leaveGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException;

  void updateGame(GameData gameData) throws DataAccessException;

  void deleteAllGames() throws DataAccessException;
}
