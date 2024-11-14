import server.Server;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        int port = 0;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        Server server = new Server();
        port = server.run(port);
        var serverURL = "http://localhost:" + port;
        if (args.length == 1) {
            serverURL = args[0];
        }

        new Repl(serverURL).run();

        server.stop();
    }
}