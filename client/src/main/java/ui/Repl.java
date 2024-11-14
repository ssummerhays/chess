package ui;

import java.util.Scanner;

public class Repl {
  private final ChessClient client;

  public Repl(String serverURL) { client = new ChessClient(serverURL);}

  public void run() {
    System.out.println("♕ Welcome to 240 Chess. Type help to begin");

    Scanner scanner = new Scanner(System.in);
    var result = "";
    while (!result.equals("quit")) {
      printPrompt();
      String line = scanner.nextLine();

      try {
        result = client.eval(line);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + result);
      } catch (Throwable e) {
        var msg = e.getMessage();
        System.out.print(msg);
      }
      System.out.println();
    }
  }

  private void printPrompt() { System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR + "[" + client.state + "] >>> "); }
}
