import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class EvalServer {
  public static final int SERVER_PORT = 8080;

  public static void main(String... args) throws IOException {
    System.out.println("Eval server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
    try {
      System.out.println("Start to accept incoming connections");

      while (true) {
        // Call accept method (blocking call).
        // When a client connects, accept method will return.
        Socket clientSocket = serverSocket.accept();
        new Thread(new Handler(clientSocket)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }
}
