package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MemoryAuthDataAccess implements AuthDataAccess {
  public Collection<AuthData> authDataList = new HashSet<>();
  public AuthData getAuth(String authToken) throws DataAccessException {
    for (AuthData authData : authDataList) {
      if (Objects.equals(authToken, authData.authToken())) {
        return authData;
      }
    }
    throw new DataAccessException("Error: unauthorized");
  }

  public void createAuth(AuthData authData) throws DataAccessException {
    for (AuthData collectionAuthData : authDataList) {
      if (Objects.equals(collectionAuthData.authToken(), authData.authToken())) {
        throw new DataAccessException("Error: already taken");
      }
    }
    authDataList.add(authData);
  }

  public void deleteAuth(AuthData authData) {
    authDataList.remove(authData);
  }

  public void deleteAllAuthTokens() {
    authDataList.clear();
  }
}
