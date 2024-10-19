package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess {
  Collection<UserData> userDataList;
  Collection<AuthData> authDataList;
  Collection<GameData> gameDataList;

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

}
