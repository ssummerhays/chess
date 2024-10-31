package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDataAccessTest {
  static MySqlUserDataAccess mySqlUserDAO;

  @BeforeAll
  public static void init() {
    try {
      mySqlUserDAO=new MySqlUserDataAccess();
      try (var conn=DatabaseManager.getConnection()) {
        String[] statements={
                "INSERT INTO userData (username, password, email) VALUES (\"testUsername1\", \"testPassword1\", \"testEmail1\");",
                "INSERT INTO userData (username, password, email) VALUES (\"testUsername2\", \"testPassword2\", \"testEmail2\");",
                "INSERT INTO userData (username, password, email) VALUES (\"testUsername3\", \"testPassword3\", \"testEmail3\");"
        };
        int counter=1;
        for (var statement : statements) {
          try (var preparedStatement=conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
            counter++;
          } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
              System.out.println("testUsername" + counter + " already Exists");
              counter++;
            } else {
              fail(e.getMessage());
            }
          }
        }
      } catch (SQLException e) {
        fail(e.getMessage());
      }
    } catch (DataAccessException e) {
      fail(e.getMessage());
    }
  }

  @Test
  @Order(1)
  @DisplayName("Positive getUser MySqlDAO test")
  void getUserPositiveSql() {
    try {
      UserData actualUserData1=mySqlUserDAO.getUser("testUsername1");
      UserData expectedUserData1=new UserData("testUsername1", "testPassword1", "testEmail1");
      UserData actualUserData2=mySqlUserDAO.getUser("testUsername2");
      UserData expectedUserData2=new UserData("testUsername2", "testPassword2", "testEmail2");

      assertEquals(expectedUserData1, actualUserData1);
      assertEquals(expectedUserData2, actualUserData2);
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @Order(2)
  @DisplayName("Negative getUser MySqlDAO test")
  void getUserNegativeSql() {
    Exception exception=assertThrows(DataAccessException.class, () -> mySqlUserDAO.getUser(""));
    assertTrue(exception.getMessage().contains("unauthorized"));
  }

  @Test
  @Order(3)
  @DisplayName("Positive createUser MySqlDAO test")
  void createUserPositiveSql() {
    try (var conn = DatabaseManager.getConnection()) {
      try (var preparedStatement = conn.prepareStatement("DELETE FROM userData WHERE username = 'createdUser'")) {
        preparedStatement.executeUpdate();
      }

      UserData userData=new UserData("createdUser", "createdPassword", "createdEmail");
      mySqlUserDAO.createUser(userData);

      String statement = "SELECT username, password, email FROM userData WHERE username='createdUser'";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        String username = rs.getString(1);
        String hashed = rs.getString(2);
        String email = rs.getString(3);

        assertEquals("createdUser", username);
        assertTrue(BCrypt.checkpw("createdPassword", hashed));
        assertEquals("createdEmail", email);
      }
    } catch (Throwable e) {
      fail();
    }
  }

  @Test
  @Order(4)
  @DisplayName("Negative createUser MySqlDAO test")
  void createUserNegativeSql() {
    UserData userData = new UserData("testUsername1", "testPassword1", "testEmail1");
    Exception e = assertThrows(DataAccessException.class, () -> mySqlUserDAO.createUser(userData));
    assertTrue(e.getMessage().contains("already taken"));
  }

  @Test
  @Order(5)
  @DisplayName("Positive deleteAllUsers MySqlDAO test")
  void deleteAllUsersPositiveSql() {
    try (var conn = DatabaseManager.getConnection()) {
      mySqlUserDAO.deleteAllUsers();
      String statement = "SELECT COUNT(*) FROM userData";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        assertEquals(0, count);
      }
    } catch (Throwable e) {
      fail();
    }
  }
}