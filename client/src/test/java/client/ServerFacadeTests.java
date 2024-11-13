package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import service.requests.CreateGameRequest;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import java.net.HttpURLConnection;


public class ServerFacadeTests {

    private static UserData existingUser;
    private static UserData newUser;
    private static CreateGameRequest createRequest;
    private String existingAuth;
    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
        RegisterResult regResult = serverFacade.register(registerRequest);
        existingAuth = regResult.authToken();

        createRequest = new CreateGameRequest(existingAuth, "testGame");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Normal User Login")
    public void successLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginResult = serverFacade.login(loginRequest);

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(existingUser.username(), loginResult.username(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }

    @Test
    @Order(2)
    @DisplayName("Login Invalid User")
    public void loginInvalidUser() {
        try {
            LoginRequest loginRequest=new LoginRequest(newUser.username(), newUser.password());
            serverFacade.login(loginRequest);
        } catch (Exception e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode(),
                    "Server response code was not 401");
            Assertions.assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Normal User Registration")
    public void successRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        RegisterResult registerResult = serverFacade.register(registerRequest);

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(newUser.username(), registerResult.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.authToken(), "Response did not contain an authentication string");
    }

    @Test
    @Order(4)
    @DisplayName("Re-Register User")
    public void registerTwice() {
        try {
            RegisterRequest registerRequest=new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
            serverFacade.register(registerRequest);
        } catch (Exception e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, serverFacade.getStatusCode());
            Assertions.assertTrue(e.getMessage().contains("already taken"));
        }
    }

    @Test
    @Order(5)
    @DisplayName("Register Bad Request")
    public void failRegister() {
        try {
            RegisterRequest registerRequest=new RegisterRequest(existingUser.username(), null, existingUser.email());
            serverFacade.register(registerRequest);
        } catch (Exception e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, serverFacade.getStatusCode());
            Assertions.assertTrue(e.getMessage().contains("bad request"));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void successLogout() {
        LogoutRequest logoutRequest = new LogoutRequest(existingAuth);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(logoutRequest));
        assertHttpOk(serverFacade.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Invalid Auth Logout")
    public void failLogout() {
        LogoutRequest logoutRequest = new LogoutRequest(existingAuth);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(logoutRequest));

        assertHttpOk(serverFacade.getStatusCode());

        Exception e = Assertions.assertThrows(Exception.class, () -> {
            serverFacade.logout(logoutRequest);
        });

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, serverFacade.getStatusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));

    }

    private void assertHttpOk(int status) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, status);
    }

}
