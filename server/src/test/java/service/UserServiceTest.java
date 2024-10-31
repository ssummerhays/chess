package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import dataaccess.MemoryUserDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
  private UserService service;
  private MemoryUserDataAccess userDAO;
  private MemoryAuthDataAccess authDAO;
  private MemoryGameDataAccess gameDAO;
  private final String newUsername = "newUsername";
  private final String existingUsername = "existingUsername";
  private final String password = "correctPassword";
  private final String email = "user@example.com";

  @BeforeEach
  public void setUp() {
    userDAO = new MemoryUserDataAccess();
    authDAO = new MemoryAuthDataAccess();
    gameDAO = new MemoryGameDataAccess();
    service = new UserService(userDAO, authDAO, gameDAO);

    UserData existingUser = new UserData(existingUsername, password, email);
    userDAO.userDataList.add(existingUser);
  }

  @Test
  @DisplayName("Successfully register new user")
  public void registerSuccess() {
    RegisterRequest req = new RegisterRequest(newUsername, password, email);
    UserData userData = new UserData(newUsername, password, email);

    try {
      RegisterResult result = service.register(req);
      String authToken = result.authToken();
      String returnedUsername = result.username();
      AuthData authData = new AuthData(authToken, newUsername);

      assertEquals(newUsername, returnedUsername);
      assertNotNull(authToken);
      assertTrue(userDAO.userDataList.contains(userData));
      assertTrue(authDAO.authDataList.contains(authData));

    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @DisplayName("Unsuccessfully register user with no password")
  public void registerFail() {
    RegisterRequest req = new RegisterRequest(newUsername, null, email);

    DataAccessException exception = assertThrows(DataAccessException.class, () -> service.register(req));
    assertEquals("Error: bad request", exception.getMessage());
    assertEquals(1, userDAO.userDataList.size());
  }

  @Test
  @DisplayName("Successfully login existing user")
  public void loginSuccess() {
    LoginRequest req = new LoginRequest(existingUsername, password);

    try {
      LoginResult result = service.login(req);
      String authToken = result.authToken();
      String returnedUsername = result.username();
      AuthData authData = new AuthData(authToken, existingUsername);

      assertEquals(existingUsername, returnedUsername);
      assertNotNull(authToken);
      assertTrue(authDAO.authDataList.contains(authData));

    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @DisplayName("Unsuccessfully login user that does not exist")
  public void loginFail() {
    LoginRequest req = new LoginRequest(newUsername, password);
    DataAccessException exception = assertThrows(DataAccessException.class, () -> service.login(req));
    assertEquals("Error: unauthorized", exception.getMessage());
    assertTrue(authDAO.authDataList.isEmpty());
  }

  @Test
  @DisplayName("Successfully logout user")
  public void logoutSuccess() {
    LoginRequest loginRequest = new LoginRequest(existingUsername, password);

    try {
      LoginResult result = service.login(loginRequest);
      String authToken = result.authToken();
      String returnedUsername = result.username();
      AuthData authData = new AuthData(authToken, existingUsername);

      assertEquals(existingUsername, returnedUsername);
      assertNotNull(authToken);
      assertTrue(authDAO.authDataList.contains(authData));

      LogoutRequest logoutRequest = new LogoutRequest(result.authToken());
      service.logout(logoutRequest);
      assertFalse(authDAO.authDataList.contains(authData));

    } catch (DataAccessException e) {
      fail();
    }

  }

  @Test
  @DisplayName("Unsuccessfully log out a logged out user")
  public void logoutFail() {
    LogoutRequest logoutRequest = new LogoutRequest("AuthTokenDoesNotExist");
    DataAccessException exception = assertThrows(DataAccessException.class, () -> service.logout(logoutRequest));
    assertEquals("Error: unauthorized", exception.getMessage());
  }

  @Test
  @DisplayName("Test Clear")
  public void clearTest() {
    GameData gameData = new GameData(1, null, null, "name", new ChessGame());
    gameDAO.gameDataList.add(gameData);

    AuthData authData = new AuthData("token", newUsername);
    authDAO.authDataList.add(authData);

    assertFalse(userDAO.userDataList.isEmpty());
    assertFalse(gameDAO.gameDataList.isEmpty());
    assertFalse(authDAO.authDataList.isEmpty());

    assertDoesNotThrow(() -> service.clear());

    assertTrue(userDAO.userDataList.isEmpty());
    assertTrue(gameDAO.gameDataList.isEmpty());
    assertTrue(authDAO.authDataList.isEmpty());
  }
}