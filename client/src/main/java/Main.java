import server.Server;
import ui.Repl;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();
        int port = server.run(0);

        String serverURL = "http://localhost:" + port;


        new Repl(serverURL).run();

        server.stop();
    }
}