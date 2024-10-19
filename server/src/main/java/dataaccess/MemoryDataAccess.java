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

}
