import server.ServerHelper;
import ui.Repl;

public class Main {
    public static void main(String[] args) {

        ServerHelper serverHelper = new ServerHelper();
        String serverURL = serverHelper.runServer(0);


        new Repl(serverURL).run();

        serverHelper.stopServer();
    }
}