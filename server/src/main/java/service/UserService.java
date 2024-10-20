package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import dataaccess.MemoryUserDataAccess;
import model.AuthData;
import model.UserData;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.RegisterResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
  public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
    String username = registerRequest.username();
    String password = registerRequest.password();
    String email = registerRequest.email();

    MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
    MemoryAuthDataAccess authDao = new MemoryAuthDataAccess();

    if (userDAO.getUser(username) != null) {
      throw new DataAccessException("Error: already taken");
    }
    UserData userData = new UserData(username, password, email);
    userDAO.createUser(userData);

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    authDao.createAuth(authData);

    return new RegisterResult(username, authToken);
  }

  public AuthData login(LoginRequest loginRequest) throws DataAccessException {
    String username = loginRequest.username();
    String password = loginRequest.password();

    MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();

    UserData userData = userDAO.getUser(username);
    if (userData == null) {
      throw new DataAccessException("Error: bad request");
    }
    if (!Objects.equals(password, userData.password())) {
      throw new DataAccessException("Error: unauthorized");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    authDAO.createAuth(authData);

    return authData;
  }

  public void logout(LogoutRequest logoutRequest) throws DataAccessException {
    String authToken = logoutRequest.authToken();
    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    AuthData authData = authDAO.getAuth(authToken);
    authDAO.deleteAuth(authData);
  }

  public void clear() {
    MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    MemoryGameDataAccess gameDAO = new MemoryGameDataAccess();

    userDAO.deleteAllUsers();
    authDAO.deleteAllAuthTokens();
    gameDAO.deleteAllGames();
  }
}
