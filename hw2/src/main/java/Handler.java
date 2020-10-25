import static java.lang.Character.isDigit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Handler implements Runnable {

  private Socket clientSocket;
  private static final int BUFFER_SIZE = 16;

  /**
   * Instantiates a new Handler.
   *
   * @param clientSocket the client socket
   */
  public Handler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try {
      //BufferedReader bufferedReader = new BufferedReader(
          //new InputStreamReader(clientSocket.getInputStream()));
      InputStream inputStream = clientSocket.getInputStream();
      byte[] result = getInputStreamBytes(inputStream);
      String request = new String(result);
      String[] req = request.split("\n");
      List<String> reqList = Arrays.asList(req);
      String[] firstLine = reqList.get(0).split(" ");

      String answer = "";
      if (firstLine[0].equals("GET") && firstLine[1].equals("/api/gettime")) {

        //while ((line = bufferedReader.readLine()) != null) {
          //System.out.println(line);
        //}

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        String s = formatter.format(date);
        String lenOfs = String.valueOf(s.length());
        answer = answer + "HTTP/1.0 200 OK\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: " + lenOfs +"\n";
        answer = answer + "\n";
        answer = answer + s;
      } else if (firstLine[0].equals("POST") && firstLine[1].equals("/api/evalexpression")) {
        int idx = reqList.indexOf("\n");
        String body = "";
        for (String s : reqList) {
          if (s.matches("[\\d|\\+|\\-\\(|\\)]+")) {
            body = s;
          }
        }
        int aws = this.eval(body);
        String line = String.valueOf(aws);
        String lenOfs = String.valueOf(line.length());
        answer = answer + "HTTP/1.0 200 OK\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: " + lenOfs +"\n";
        answer = answer + "\n";
        answer = answer + line;

      }
      OutputStream outputStream = clientSocket.getOutputStream();

      byte[] answerToBytes = answer.getBytes();
      outputStream.write(answerToBytes);

      clientSocket.close();
  } catch (IOException e) {
      e.printStackTrace();
    }


  }

  /**
   * evaluate an expression
   * @param stringOfExpression
   * @return answer
   */
  private int eval(String stringOfExpression) {
    int answer = 0;
    int tmpAnswer = 0;
    int sign = 1;
    for (int x = 0; x < stringOfExpression.length(); x++) {
      if (isDigit(stringOfExpression.charAt(x))) {
        tmpAnswer = tmpAnswer * 10 + Integer.parseInt(String.valueOf(stringOfExpression.charAt(x)));
      } else if (stringOfExpression.charAt(x) == '-') {
        answer += sign * tmpAnswer;
        tmpAnswer = 0;
        sign = -1;
      } else {
        answer += sign * tmpAnswer;
        tmpAnswer = 0;
        sign = 1;
      }
    }
    answer += sign * tmpAnswer;
    return answer;
  }

  private byte[] getInputStreamBytes(InputStream inputStream) throws IOException {
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

