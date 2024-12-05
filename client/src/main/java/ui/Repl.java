package ui;

import websocket.NotificationHandler;
import websocket.messages.Notification;

import java.util.Scanner;

import static ui.EscapeSequences.ERASE_LINE;

public class Repl implements NotificationHandler {
  private final ChessClient client;

  public Repl(String serverURL) { client = new ChessClient(serverURL, this);}

  public void run() {
    System.out.println("♕ Welcome to 240 Chess. Type help to begin");

    Scanner scanner = new Scanner(System.in);
    var result = "init";
    while (!result.equals("quit")) {
      if (!result.isEmpty()) {
        printPrompt();
      }
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

  public void notify(Notification notification) {
    System.out.print(ERASE_LINE + '\r');
    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + notification.getMessage());
    printPrompt();
  }

  private void printPrompt() { System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR + "[" + client.state + "] >>> "); }
}
