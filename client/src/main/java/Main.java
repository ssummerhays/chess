import server.Server;
import ui.ChessClient;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server();
        server.run(port);
        var serverURL = "http://localhost:" + port;
        if (args.length == 1) {
            serverURL = args[0];
        }

        ChessClient client = new ChessClient(serverURL, server.userDataAccess, server.authDataAccess, server.gameDataAccess);
        new Repl(client).run();

        server.stop();
    }
}