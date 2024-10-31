package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.PrintedGameData;

import java.util.Collection;

public class MySqlGameDataAccess implements GameDataAccess {
  public Collection<PrintedGameData> getGames() throws DataAccessException {
    return null;
  }

  public GameData getGame(int gameID) throws DataAccessException {
    return null;
  }

  public int createGame(String gameName) throws DataAccessException {
    return 0;
  }
  
  public void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException {

  }

  public void deleteAllGames() throws DataAccessException {

  }
}
