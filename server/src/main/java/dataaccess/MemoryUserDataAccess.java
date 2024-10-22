package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryUserDataAccess implements UserDataAccess {
  public Collection<UserData> userDataList = new HashSet<>();

  public UserData getUser(String username) {
    for (UserData userData : userDataList) {
      if (Objects.equals(username, userData.username())) {
        return userData;
      }
    }
    return null;
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
