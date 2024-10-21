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
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
  public final MemoryUserDataAccess userDAO;
  public final MemoryAuthDataAccess authDAO;
  public final MemoryGameDataAccess gameDAO;

  public UserService(MemoryUserDataAccess userDAO, MemoryAuthDataAccess authDAO, MemoryGameDataAccess gameDAO) {
    this.userDAO = userDAO;
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }
  public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
    String username = registerRequest.username();
    String password = registerRequest.password();
    String email = registerRequest.email();

    if (username == null || password == null || email == null) {
      throw new DataAccessException("Error: bad request");
    }

    if (userDAO.getUser(username) != null) {
      throw new DataAccessException("Error: already taken");
    }
    UserData userData = new UserData(username, password, email);
    userDAO.createUser(userData);

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    authDAO.createAuth(authData);

    return new RegisterResult(username, authToken);
  }

  public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
    String username = loginRequest.username();
    String password = loginRequest.password();

    UserData userData = userDAO.getUser(username);
    if (userData == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    if (!Objects.equals(password, userData.password())) {
      throw new DataAccessException("Error: unauthorized");
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    authDAO.createAuth(authData);

    return new LoginResult(username, authToken);
  }

  public void logout(LogoutRequest logoutRequest) throws DataAccessException {
    String authToken = logoutRequest.authToken();
    AuthData authData = authDAO.getAuth(authToken);
    authDAO.deleteAuth(authData);
  }

  public void clear() {
    userDAO.deleteAllUsers();
    authDAO.deleteAllAuthTokens();
    gameDAO.deleteAllGames();
  }
}
