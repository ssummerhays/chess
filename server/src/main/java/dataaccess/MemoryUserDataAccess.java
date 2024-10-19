package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Objects;

public class MemoryUserDataAccess implements UserDataAccess {
  Collection<UserData> userDataList;

  public UserData getUser(String username) throws DataAccessException {
    for (UserData userData : userDataList) {
      if (Objects.equals(username, userData.username())) {
        return userData;
      }
    }
    throw new DataAccessException("Error: bad request");
  }

  public void createUser(UserData userData) throws DataAccessException {
    for (UserData collectionUserData : userDataList) {
      if (Objects.equals(collectionUserData.username(), userData.username())) {
        throw new DataAccessException("Error: already taken");
      }
    }
    userDataList.add(userData);
  }

  public void deleteAllUsers() {
    userDataList.clear();
  }
}
