import ui.PreLoginRepl;

public class Main {
    public static void main(String[] args) {
        var serverURL = "http://localhost:8080";
        if (args.length == 1) {
            serverURL = args[0];
        }

        new PreLoginRepl(serverURL).run();
    }
}