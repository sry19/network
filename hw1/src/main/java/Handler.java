import static java.lang.Character.isDigit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Handler implements Runnable {

  private Socket clientSocket;

  public Handler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
    System.out.println(String.format("Handle client %s", client));
    try {

        InputStream inputStream = this.clientSocket.getInputStream();

        byte[] result = new byte[0];
        int i = 0;
        byte[] tmp = new byte[16];
        while((i = inputStream.read(tmp,0, 16))==16) {
          byte[] newResult = new byte[result.length + i];
          System.arraycopy(result, 0, newResult, 0, result.length);
          System.arraycopy(tmp, 0, newResult, result.length, i);
          result = newResult;
        }

        if (i != -1) {
          byte[] newResult = new byte[result.length+i];
          System.arraycopy(result,0,newResult,0,result.length);
          System.arraycopy(tmp,0,newResult,result.length,i);
          result = newResult;
        }
        List<String> expressions = new ArrayList<>();
        int idx = 0;
        int l = (int)result[idx+1] & 0xFF;
        l += ((int)result[idx] & 0xFF) << 8;
        System.out.println(l);

        byte[] output = new byte[2];
        output[0] = (byte) (l >> 8 & 0xFF);
        output[1] = (byte) (l & 0xFF);

        idx = idx + 2;
        while (l > 0) {
          int lenOfExpression = (int) result[idx + 1] & 0xFF;
          lenOfExpression += ((int) result[idx] & 0xFF) << 8;
          idx += 2;
          byte[] expression = Arrays.copyOfRange(result, idx, idx + lenOfExpression);
          String stringOfExpression = new String(expression);
          int answer = 0;
          int tmpAnswer = 0;
          int sign = 1;
          for (int x = 0; x < stringOfExpression.length(); x++) {
            if (isDigit(stringOfExpression.charAt(x))) {
              tmpAnswer = tmpAnswer * 10 + Integer.parseInt(String.valueOf(stringOfExpression.charAt(x)));
            } else if (stringOfExpression.charAt(x) == '-') {
              answer += sign*tmpAnswer;
              tmpAnswer = 0;
              sign = -1;
            } else {
              answer += sign*tmpAnswer;
              tmpAnswer = 0;
              sign = 1;
            }
          }
          answer += sign*tmpAnswer;
          System.out.println(answer);
          idx += lenOfExpression;
          l--;
        }

        clientSocket.close();
      } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
