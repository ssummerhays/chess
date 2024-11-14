package dataaccess;

import model.UserData;

public interface UserDataAccess {
  UserData getUser(String username) throws DataAccessException;

  void createUser(UserData userData) throws DataAccessException;

  void deleteAllUsers() throws DataAccessException;
}