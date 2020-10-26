
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * The type Handler.
 */
public class Handler implements Runnable {

  private Socket clientSocket;
  private List<Date> evalexpressions;
  private List<Date> gettime;
  private List<String> expressions;
  private List<String> reqList;

  /**
   * Instantiates a new Handler.
   *
   * @param clientSocket    the client socket
   * @param evalexpressions the evalexpressions
   * @param gettime         the gettime
   * @param expressions     the expressions
   * @param reqList         the req list
   */
  public Handler(Socket clientSocket, List<Date> evalexpressions, List<Date> gettime,
      List<String> expressions, List<String> reqList) {
    this.clientSocket = clientSocket;
    this.evalexpressions = evalexpressions;
    this.gettime = gettime;
    this.expressions = expressions;
    this.reqList = reqList;
  }

  @Override
  public void run() {
    try {
      String[] firstLine = this.reqList.get(0).split(" ");

      String answer = "";
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
      Date date = new Date(System.currentTimeMillis());
      if (firstLine[0].equals("GET") && firstLine[1].equals("/api/gettime")) {

        String s = formatter.format(date);
        String lenOfs = String.valueOf(s.length());
        answer = answer + "HTTP/1.0 200 OK\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: " + lenOfs + "\n";
        answer = answer + "\n";
        answer = answer + s;
        answer = answer + "\n";
      } else if (firstLine[0].equals("POST") && firstLine[1].equals("/api/evalexpression")) {
        String body = "";
        for (String s : this.reqList) {
          if (s.matches("[\\d|\\+|\\-\\(|\\)]+")) {
            body = s;
          }
        }
        int aws = this.eval(body);
        String line = String.valueOf(aws);
        String lenOfs = String.valueOf(line.length());
        if (body.equals("") || (!isValid(body))) {
          answer = answer + "HTTP/1.0 400 Bad Request\n";
        } else {
          answer = answer + "HTTP/1.0 200 OK\n";
        }
        answer = answer + "Content-Type: text/html\n";
        if (body.equals("") || (!isValid(body))) {
          answer = answer + "Content-Length: 11" + "\n";
          answer = answer + "\n";
          answer = answer + "Bad Request";
        } else {
          answer = answer + "Content-Length: " + lenOfs + "\n";
          answer = answer + "\n";
          answer = answer + line;
        }
        answer = answer + "\n";
      } else if (firstLine[0].equals("GET") && firstLine[1].equals("/status.html")) {
        int countForLastMinuteGT = 0;
        int countForLastHourGT = 0;
        int countForLast24HourGT = 0;
        int countLifeTimeGT = 0;
        for (Date prev : this.gettime) {
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
        for (Date prev : this.evalexpressions) {
          if ((date.getTime() - prev.getTime()) / 1000 < 60) {
            countForLastMinuteEV = countForLastMinuteEV + 1;
            countForLastHourEV = countForLastHourEV + 1;
            countForLast24HourEV = countForLast24HourEV + 1;
            countLifeTimeEV = countLifeTimeEV + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60 * 60) {
            countForLastHourEV = countForLastHourEV + 1;
            countForLast24HourEV = countForLast24HourEV + 1;
            countLifeTimeEV = countLifeTimeEV + 1;
          } else if ((date.getTime() - prev.getTime()) / 1000 < 60 * 60 * 24) {
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
        answer = answer + "Content-Length: " + lenOfs + "\n";
        answer = answer + "\n";
        answer = answer + res;
        answer = answer + "\n";
      } else {
        answer = answer + "HTTP/1.0 404 Not Found\n";
        answer = answer + "Content-Type: text/html\n";
        answer = answer + "Content-Length: 9" + "\n";
        answer = answer + "\n";
        answer = answer + "Not Found";
        answer = answer + "\n";
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
   *
   * @param s
   * @return answer
   */
  private int eval(String s) {
    if (s == null) {
      return 0;
    }
    Queue<Character> q = new LinkedList<>();
    for (char c : s.toCharArray()) {
      q.offer(c);
    }
    q.offer('+');
    return cal(q);
  }

  private int cal(Queue<Character> q) {
    char sign = '+';
    int num = 0;
    Stack<Integer> stack = new Stack<>();
    while (!q.isEmpty()) {
      char c = q.poll();
      if (c == ' ') {
        continue;
      }
      if (Character.isDigit(c)) {
        num = 10 * num + c - '0';
      } else if (c == '(') {
        num = cal(q);
      } else {
        if (sign == '+') {
          stack.push(num);
        } else if (sign == '-') {
          stack.push(-num);
        }
        num = 0;
        sign = c;
        if (c == ')') {
          break;
        }
      }
    }
    int sum = 0;
    while (!stack.isEmpty()) {
      sum += stack.pop();
    }
    return sum;
  }

  private boolean isValid(String s) {
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

