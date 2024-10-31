package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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
  @DisplayName("Negative getUser MySqlDAO test")
  void getUserNegativeSql() {
    Exception exception=assertThrows(DataAccessException.class, () -> mySqlUserDAO.getUser(""));
    assertTrue(exception.getMessage().contains("unauthorized"));
  }

  @Test
  @DisplayName("Positive createUser MySqlDAO test")
  void createUserPositiveSql() {
  }

  @Test
  @DisplayName("Negative createUser MySqlDAO test")
  void createUserNegativeSql() {
  }

  @Test
  @DisplayName("Positive deleteAllUsers MySqlDAO test")
  void deleteAllUsersPositiveSql() {
  }
}