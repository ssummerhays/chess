package dataaccess;

import model.AuthData;

public class MySqlAuthDataAccess implements AuthDataAccess {
  public AuthData getAuth(String authToken) throws DataAccessException {
    return null;
  }

  public void createAuth(AuthData authData) throws DataAccessException {

  }

  public void deleteAuth(AuthData authData) {

  }

  public void deleteAllAuthTokens() {

  }
}
