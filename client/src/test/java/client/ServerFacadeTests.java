package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import service.requests.CreateGameRequest;
import service.requests.LoginRequest;
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

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, serverFacade.getStatusCode(),
                "Server response code was not 200 OK");

        Assertions.assertEquals(existingUser.username(), loginResult.username(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }

}
