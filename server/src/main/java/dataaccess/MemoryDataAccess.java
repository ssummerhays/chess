package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess {
  Collection<UserData> userDataList;
  Collection<AuthData> authDataList;
  Collection<GameData> gameDataList;
  int nextGameID = 1;

  public UserData getUser(String username) throws DataAccessException {
    for (UserData userData : userDataList) {
      if (username == userData.username()) {
        return userData;
      }
    }
    throw new DataAccessException("Error: bad request");
  }

  public void createUser(UserData userData) throws DataAccessException {
    for (UserData collectionUserData : userDataList) {
      if (collectionUserData.username() == userData.username()) {
        throw new DataAccessException("Error: already taken");
      }
    }
    userDataList.add(userData);
  }

  public void deleteAllUsers() {
    userDataList.clear();
  }

  public AuthData getAuth(String authToken) throws DataAccessException {
    for (AuthData authData : authDataList) {
      if (authToken == authData.authToken()) {
        return authData;
      }
    }
    throw new DataAccessException("Error: bad request");
  }

  public void createAuth(AuthData authData) throws DataAccessException {
    for (AuthData collectionAuthData : authDataList) {
      if (collectionAuthData.authToken() == authData.authToken()) {
        throw new DataAccessException("Error: already taken");
      }
    }
  }

  public void deleteAuth(AuthData authData) {
    authDataList.remove(authData);
  }

  public void deleteAllAuthTokens() {
    authDataList.clear();
  }

  public Collection<GameData> getGames() {
    return gameDataList;
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
      if (collectionGameData.gameName() == gameName) {
        throw new DataAccessException("Error: bad request");
      }
    }

    int gameID = nextGameID;
    nextGameID++;
    GameData gameData = new GameData(gameID, "", "", gameName, new ChessGame());
    gameDataList.add(gameData);
    return gameID;
  }

  public void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
    if (!gameDataList.contains(gameData)) {
      throw new DataAccessException("Error: bad request");
    }
    if (teamColor == ChessGame.TeamColor.WHITE) {
      if (gameData.whiteUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    } else {
      if (gameData.blackUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      gameDataList.remove(gameData);
      gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
      gameDataList.add(gameData);
    }
  }

  public void deleteAllGames() {
    gameDataList.clear();
  }

}
