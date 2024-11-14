import ui.Repl;

public class Main {
    public static void main(String[] args) {
        int port = 0;

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        String serverURL = "http://localhost:" + port;


        new Repl(serverURL).run();
    }
}