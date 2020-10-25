import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class EvalServer {

  public static final int SERVER_PORT = 8080;
  private static final int BUFFER_SIZE = 16;

  public static void main(String... args) throws IOException {
    System.out.println("Eval server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
    try {
      System.out.println("Start to accept incoming connections");

      while (true) {
        // Call accept method (blocking call).
        // When a client connects, accept method will return.
        Socket clientSocket = serverSocket.accept();
        //InputStream inputStream = clientSocket.getInputStream();
        //byte[] result = getInputStreamBytes(inputStream);
        //System.out.println(new String(result));
        new Thread(new Handler(clientSocket)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }

  private static byte[] getInputStreamBytes(InputStream inputStream) throws IOException {
    byte[] result = new byte[0];
    int i = 0;
    byte[] tmp = new byte[BUFFER_SIZE];
    while ((i = inputStream.read(tmp, 0, BUFFER_SIZE)) == BUFFER_SIZE) {
      byte[] newResult = new byte[result.length + i];
      System.arraycopy(result, 0, newResult, 0, result.length);
      System.arraycopy(tmp, 0, newResult, result.length, i);
      result = newResult;
    }
    if (i != -1) {
      byte[] newResult = new byte[result.length + i];
      System.arraycopy(result, 0, newResult, 0, result.length);
      System.arraycopy(tmp, 0, newResult, result.length, i);
      result = newResult;
    }
    return result;
  }
}
