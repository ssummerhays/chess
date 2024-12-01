package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryGameDataAccess implements GameDataAccess {
  public Collection<GameData> gameDataList = new HashSet<>();
  public int nextGameID = 1;

  public Collection<GameData> getGames() {
    Collection<GameData> resultGamesData = new HashSet<>();
    for (GameData gameData : gameDataList) {
      GameData resultGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
              gameData.gameName(), gameData.game());
      resultGamesData.add(resultGameData);
    }
    return resultGamesData;
  }

  public GameData getGame(int gameID) throws DataAccessException {
    for (GameData gameData : gameDataList) {
      if (gameData.gameID() == gameID) {
        return gameData;
      }
    }
    throw new DataAccessException("Error: bad request");
  }

  public  int createGame(String gameName) throws DataAccessException {
    for (GameData collectionGameData : gameDataList) {
      if (Objects.equals(collectionGameData.gameName(), gameName)) {
        throw new DataAccessException("Error: bad request");
      }
    }

    int gameID = nextGameID;
    nextGameID++;
    GameData gameData = new GameData(gameID, null, null, gameName, new ChessGame());
    gameDataList.add(gameData);
    return gameID;
  }

  public void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
    if (!gameDataList.contains(gameData)) {
      throw new DataAccessException("Error: bad request");
    }
    if (teamColor == ChessGame.TeamColor.WHITE) {
      if (!Objects.equals(gameData.whiteUsername(), null)) {
        throw new DataAccessException("Error: already taken");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    } else {
      if (!Objects.equals(gameData.blackUsername(), null)) {
        throw new DataAccessException("Error: already taken");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    }
  }

  public void leaveGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
    if (!gameDataList.contains(gameData)) {
      throw new DataAccessException("Error: bad request");
    }
    if (teamColor == ChessGame.TeamColor.WHITE) {
      if (!Objects.equals(gameData.whiteUsername(), username)) {
        throw new DataAccessException("Error: bad request");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    } else {
      if (!Objects.equals(gameData.blackUsername(), username)) {
        throw new DataAccessException("Error: bad request");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    }
  }

  public void deleteAllGames() {
    gameDataList.clear();
  }
}
