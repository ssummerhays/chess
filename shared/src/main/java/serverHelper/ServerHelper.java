package serverHelper;

import server.Server;

public class ServerHelper {
  Server server;

  public ServerHelper() {
    server = new Server();
  }
  public String runServer(int port) {
    port = server.run(port);
    return "http://localhost:" + port;
  }
  public void stopServer() {
    server.stop();
  }
}
