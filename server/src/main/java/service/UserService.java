package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.util.Objects;
import java.util.UUID;

public class UserService {
  public final UserDataAccess userDAO;
  public final AuthDataAccess authDAO;
  public final GameDataAccess gameDAO;

  public UserService(UserDataAccess userDAO, AuthDataAccess authDAO, GameDataAccess gameDAO) {
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
      if (userDAO.getClass() == MemoryUserDataAccess.class) {
        throw new DataAccessException("Error: unauthorized");
      }
      if (!BCrypt.checkpw(password, userData.password())) {
        throw new DataAccessException("Error: unauthorized");
      }
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

  public void clear() throws DataAccessException {
    userDAO.deleteAllUsers();
    authDAO.deleteAllAuthTokens();
    gameDAO.deleteAllGames();
  }
}
