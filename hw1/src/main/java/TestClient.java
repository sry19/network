import static java.lang.Character.isDigit;
import static java.lang.Integer.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * The type Test client.
 */
public class TestClient {

  /**
   * The constant SERVER_PORT.
   */
  public static final int SERVER_PORT = 8080;
  public static final int BUFFER_SIZE = 16;

  private static byte[] convertIntToBigEndian(int l) {
    byte[] output = new byte[2];
    output[0] = (byte) (l >> 8 & 0xFF);
    output[1] = (byte) (l & 0xFF);
    return output;
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String... args) {
    System.out.println("Test Client");
    try {
      // InetAddress local = InetAddress.getLocalHost();
      // Can use InetAddress.getByName().
      //InetAddress local = InetAddress.getByName("192.168.0.107");
      InetAddress local = InetAddress.getLocalHost();
      System.out.println("LocalHost: " + local);
      System.out.println("");
      Socket clientSocket = new Socket(local, SERVER_PORT);

      String[] testCase1 = {"1-3+4",
          "12345+12345-54321",
          "98765",
          "1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1",
          "2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2+2"
      };
      TestClient.check(testCase1, clientSocket);

      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void check(String[] testCase, Socket clientSocket) throws IOException {
    int lengthOfExpressions = testCase.length;
    byte[] result = TestClient.convertIntToBigEndian(lengthOfExpressions);
    for (int i = 0; i < lengthOfExpressions; i++) {
      int lengthOfExpression = testCase[i].length();
      byte[] bytesOfLengthOfExpression = TestClient.convertIntToBigEndian(lengthOfExpression);
      byte[] expression = testCase[i].getBytes();
      byte[] tempResult = new byte[result.length + bytesOfLengthOfExpression.length
          + expression.length];

      System.arraycopy(result, 0, tempResult, 0, result.length);
      System.arraycopy(bytesOfLengthOfExpression, 0, tempResult, result.length,
          bytesOfLengthOfExpression.length);
      System.arraycopy(expression, 0, tempResult, result.length + bytesOfLengthOfExpression.length,
          expression.length);
      result = tempResult;

    }

    OutputStream outputStream = clientSocket.getOutputStream();
    int start = 0;
    while (start < result.length) {
      byte[] tmp = Arrays.copyOfRange(result, start, min(start + BUFFER_SIZE, result.length));
      outputStream.write(tmp);
      start += BUFFER_SIZE;
    }

    InputStream inputStream = clientSocket.getInputStream();
    byte[] response = TestClient.getInputStreamBytes(inputStream);

    TestClient.handler(response, testCase);

  }

  /**
   * evaluate an expression
   * @param stringOfExpression
   * @return answer
   */
  private static int eval(String stringOfExpression) {
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

  private static int convertBigEndianToInt(byte[] bigEndian) {
    int l = (int) bigEndian[1] & 0xFF;
    l += ((int) bigEndian[0] & 0xFF) << 8;
    return l;
  }

  /**
   * Handle one test case.
   *
   * @param result   the result
   * @param testcase the testcase
   */
  public static void handler(byte[] result, String[] testcase) {

    int idx = 0;
    byte[] numberOfExpression = Arrays.copyOfRange(result, idx, idx + 2);
    int l = TestClient.convertBigEndianToInt(numberOfExpression);
    int ori = l;

    idx = idx + 2;
    while (l > 0) {
      byte[] lengthOfExpression = Arrays.copyOfRange(result, idx, idx + 2);
      int lenOfExpression = TestClient.convertBigEndianToInt(lengthOfExpression);

      idx += 2;
      byte[] expression = Arrays.copyOfRange(result, idx, idx + lenOfExpression);
      String stringOfExpression = new String(expression);
      System.out.println("Expression:" + testcase[ori - l]);
      System.out.println("Expected result:" + TestClient.eval(testcase[ori - l]));
      System.out.println("Actual result:" + stringOfExpression);
      System.out.println("");
      idx += lenOfExpression;
      l--;
    }
  }

  /**
   * convert input stream to byte array
   * @param inputStream
   * @return byte array
   * @throws IOException
   */
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
