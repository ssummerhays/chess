import chess.*;
import dataaccess.*;
import server.Server;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        try {
            var port=8080;
            if (args.length > 1) {
                port=Integer.parseInt(args[0]);
            }

            UserDataAccess userDataAccess = new MemoryUserDataAccess();
            AuthDataAccess authDataAccess = new MemoryAuthDataAccess();
            GameDataAccess gameDataAccess = new MemoryGameDataAccess();
            if (args.length >= 2 && args[1].equals("sql")) {
                userDataAccess= new MySqlUserDataAccess();
                authDataAccess = new MySqlAuthDataAccess();
                gameDataAccess = new MySqlGameDataAccess();
            }

            var userService = new UserService(userDataAccess, authDataAccess, gameDataAccess);
            var gameService = new GameService(authDataAccess, gameDataAccess);
            var serverPort = new Server(userService, gameService).run(port);

        } catch (Throwable e) {
            System.out.printf("Unable to start server: %s%n", e.getMessage());
        }
    }
}