import static java.lang.Character.isDigit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

public class Handler implements Runnable {

  private Socket clientSocket;
  private static final int BUFFER_SIZE = 16;
  private List<Date> evalexpressions;
  private List<Date> gettime;
  private List<String> expressions;

  /**
   * Instantiates a new Handler.
   *
   * @param clientSocket the client socket
   */
  /**
  public Handler(Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.evalexpressions = new ArrayList<>();
    this.gettime = new ArrayList<>();
    this.expressions = new ArrayList<>();
  }
    **/
  public Handler() {
      this.evalexpressions = new ArrayList<>();
      this.gettime = new ArrayList<>();
      this.expressions = new ArrayList<>();
  }

  public Handler setSocket(Socket clientSocket) {
     this.clientSocket = clientSocket;
     return this;
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
      SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
      Date date = new Date(System.currentTimeMillis());
      if (firstLine[0].equals("GET") && firstLine[1].equals("/api/gettime")) {

        //while ((line = bufferedReader.readLine()) != null) {
          //System.out.println(line);
        //}


        String s = formatter.format(date);
        String lenOfs = String.valueOf(s.length());
        answer = answer + "HTTP/1.0 200 OK\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: " + lenOfs +"\n";
        answer = answer + "\n";
        answer = answer + s;
        this.gettime.add(date);
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
        this.evalexpressions.add(date);
        this.expressions.add(body);
      } else if (firstLine[0].equals("GET") && firstLine[1].equals("/status.html")) {
        int countForLastMinuteGT = 0;
        int countForLastHourGT = 0;
        int countForLast24HourGT = 0;
        int countLifeTimeGT = 0;
        for (Date prev: this.gettime) {
          if ((date.getTime() - prev.getTime()) / 1000 < 60) {
            countForLastMinuteGT = countForLastMinuteGT + 1;
            countForLastHourGT = countForLastHourGT + 1;
            countForLast24HourGT = countForLast24HourGT + 1;
            countLifeTimeGT = countLifeTimeGT + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60 * 60) {
            countForLastHourGT = countForLastHourGT + 1;
            countForLast24HourGT = countForLast24HourGT + 1;
            countLifeTimeGT = countLifeTimeGT + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60 * 60 * 24) {
            countForLast24HourGT = countForLast24HourGT + 1;
            countLifeTimeGT = countLifeTimeGT + 1;
          } else {
            countLifeTimeGT = countLifeTimeGT + 1;
          }
        }
        int countForLastMinuteEV = 0;
        int countForLastHourEV = 0;
        int countForLast24HourEV = 0;
        int countLifeTimeEV = 0;
        System.out.println();
        for (Date prev: this.evalexpressions) {
          if ((date.getTime() - prev.getTime()) / 1000 < 60) {
            countForLastMinuteEV = countForLastMinuteEV + 1;
            countForLastHourEV = countForLastHourEV + 1;
            countForLast24HourEV = countForLast24HourEV + 1;
            countLifeTimeEV = countLifeTimeEV + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60*60) {
            countForLastHourEV = countForLastHourEV + 1;
            countForLast24HourEV = countForLast24HourEV + 1;
            countLifeTimeEV = countLifeTimeEV + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60*60*24) {
            countForLast24HourEV = countForLast24HourEV + 1;
            countLifeTimeEV = countLifeTimeEV + 1;
          } else {
            countLifeTimeEV = countLifeTimeEV + 1;
          }
        }
        String res = "<h1>API count information</h1>";
        res = res + "<h3>/api/evalexpression</h3>";
        res = res + "<ul>";
        res = res + String.format("<li>last minute: %d</li>", countForLastMinuteEV);
        res = res + String.format("<li>last hour: %d</li>", countForLastHourEV);
        res = res + String.format("<li>last 24 hours: %d</li>", countForLast24HourEV);
        res = res + String.format("<li>lifetime: %d</li>", countLifeTimeEV);
        res = res + "</ul>";
        res = res + "<h3>/api/gettime</h3>";
        res = res + "<ul>";
        res = res + String.format("<li>last minute: %d</li>", countForLastMinuteGT);
        res = res + String.format("<li>last hour: %d</li>", countForLastHourGT);
        res = res + String.format("<li>last 24 hours: %d</li>", countForLast24HourGT);
        res = res + String.format("<li>lifetime: %d</li>", countLifeTimeGT);
        res = res + "</ul>";
        res = res + "<h1>Last 10 expressions</h1>";
        res = res + "<ul>";
        for (int i = 0; i < 10; i++) {
          if (this.expressions.size() - i - 1 >= 0) {

            res = res + String
                .format("<li>%s</li>", this.expressions.get(this.expressions.size() - i - 1));
          }
        }
        res = res + "</ul>";
        String lenOfs = String.valueOf(res.length());
        answer = answer + "HTTP/1.0 200 OK\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: " + lenOfs +"\n";
        answer = answer + "\n";
        answer = answer + res;
      } else {
        answer = answer + "HTTP/1.0 404 Not Found\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: 9" + "\n";
        answer = answer + "\n";
        answer = answer + "Not Found";
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

