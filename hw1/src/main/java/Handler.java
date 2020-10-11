import static java.lang.Character.isDigit;
import static java.lang.Integer.min;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * The type Handler.
 */
public class Handler implements Runnable {

  private Socket clientSocket;
  public static final int BUFFER_SIZE = 16;

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
    String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
    System.out.println(String.format("Handle client %s", client));
    try {

      InputStream inputStream = this.clientSocket.getInputStream();

      byte[] result = this.getInputStreamBytes(inputStream);

      int idx = 0;
      byte[] numberOfExpression = Arrays.copyOfRange(result, idx, idx + 2);
      int l = this.convertBigEndianToInt(numberOfExpression);

      byte[] output = this.convertIntToBigEndian(l);

      idx = idx + 2;
      while (l > 0) {
        byte[] lengthOfExpression = Arrays.copyOfRange(result, idx, idx + 2);
        int lenOfExpression = this.convertBigEndianToInt(lengthOfExpression);
        idx += 2;
        byte[] expression = Arrays.copyOfRange(result, idx, idx + lenOfExpression);
        String stringOfExpression = new String(expression);
        System.out.println("Expression:" + stringOfExpression);
        int answer = this.eval(stringOfExpression);
        System.out.println("Response:" + answer);

        String stringAnswer = String.valueOf(answer);
        int lengthOfAnswer = stringAnswer.length();

        byte[] bytesOfLengthOfAnswer = this.convertIntToBigEndian(lengthOfAnswer);

        byte[] answerToBytes = stringAnswer.getBytes();
        byte[] newOutput = new byte[output.length + bytesOfLengthOfAnswer.length
            + answerToBytes.length];
        System.arraycopy(output, 0, newOutput, 0, output.length);
        System.arraycopy(bytesOfLengthOfAnswer, 0, newOutput, output.length,
            bytesOfLengthOfAnswer.length);
        System.arraycopy(answerToBytes, 0, newOutput, output.length + bytesOfLengthOfAnswer.length,
            answerToBytes.length);
        output = newOutput;

        idx += lenOfExpression;
        l--;
      }
      OutputStream outputStream = clientSocket.getOutputStream();
      int start = 0;
      while (start < output.length) {
        byte[] tmp = Arrays.copyOfRange(output, start, min(start + BUFFER_SIZE, output.length));
        outputStream.write(tmp);
        start += BUFFER_SIZE;
      }

      clientSocket.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private int convertBigEndianToInt(byte[] bigEndian) {
    int l = (int) bigEndian[1] & 0xFF;
    l += ((int) bigEndian[0] & 0xFF) << 8;
    return l;
  }

  private byte[] convertIntToBigEndian(int l) {
    byte[] output = new byte[2];
    output[0] = (byte) (l >> 8 & 0xFF);
    output[1] = (byte) (l & 0xFF);
    return output;
  }

  /**
   * convert input stream to byte array
   * @param inputStream
   * @return byte array
   * @throws IOException
   */
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
}
