package dataaccess;

import model.AuthData;

public interface AuthDataAccess {
  AuthData getAuth(String authToken) throws DataAccessException;

  void createAuth(AuthData authData) throws DataAccessException;

  void deleteAuth(AuthData authData);

  void deleteAllAuthTokens() throws DataAccessException;
}
