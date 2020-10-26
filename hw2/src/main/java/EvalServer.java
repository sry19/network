import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type Eval server.
 */
public class EvalServer {

  /**
   * The constant SERVER_PORT.
   */
  public static final int SERVER_PORT = 8181;

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws IOException the io exception
   */
  public static void main(String... args) throws IOException {
    System.out.println("Eval server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

    List<Date> evalexpressions = new ArrayList<>();
    List<Date> gettime = new ArrayList<>();
    List<String> expressions = new ArrayList<>();

    try {
      System.out.println("Start to accept incoming connections");
      while (true) {
        // Call accept method (blocking call).
        // When a client connects, accept method will return.
        Socket clientSocket = serverSocket.accept();

        String line;
        List<String> reqList = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        int lenOfContent = 0;

        while (true) {
          line = bufferedReader.readLine();
          if (line.length() == 0) {
            break;
          }
          reqList.add(line);
          if (line.startsWith("Content-Length: ")) {
            lenOfContent = Integer.parseInt(line.substring(16));
          }
        }
        char[] b = new char[lenOfContent];
        bufferedReader.read(b,0,lenOfContent);
        reqList.add(new String(b));
        String[] firstLine = reqList.get(0).split(" ");

        Date date = new Date(System.currentTimeMillis());

        if (firstLine[0].equals("GET") && firstLine[1].equals("/api/gettime")) {
          gettime.add(date);
        } else if (firstLine[0].equals("POST") && firstLine[1].equals("/api/evalexpression")) {
          String body = "";
          for (String s : reqList) {
            if (s.matches("[\\d|\\+|\\-\\(|\\)]+")) {
              body = s;
            }
          }
          if (!body.equals("") && isValid(body)) {
            evalexpressions.add(date);
            expressions.add(body);
          }
        }

        new Thread(new Handler(clientSocket, evalexpressions, gettime, expressions, reqList))
            .start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }

  private static boolean isValid(String s) {
    int l = 0;
    int r = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '(') {
        l = l + 1;
      } else if (s.charAt(i) == ')') {
        r = r + 1;
      }
      if (r > l) {
        return false;
      }
    }
    if (l != r) {
      return false;
    }
    for (int i = 0; i < s.length() - 1; i++) {
      if (s.charAt(i) == '+' || s.charAt(i) == '-') {
        if (s.charAt(i + 1) == '+' || s.charAt(i + 1) == '-' || s.charAt(i + 1) == ')') {
          return false;
        }
      }
    }
    return true;
  }
}
