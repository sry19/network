import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import org.graalvm.compiler.bytecode.Bytes;

public class EvalServer {
  public static final int SERVER_PORT = 8082;

  public static void main(String... args) throws IOException {
    System.out.println("Eval server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
    try {
      System.out.println("Start to accept incoming connections");
      String x = "Hello World!";
      System.out.println(x);
      System.out.println(x.length());
      System.out.println(new String(x.getBytes()));
      while (true) {
        // Call accept method (blocking call).
        // When a client connects, accept method will return.
        Socket clientSocket = serverSocket.accept();
        InputStream inputStream = clientSocket.getInputStream();

        byte[] result = new byte[0];
        int i = 0;
        byte[] tmp = new byte[16];
        while((i = inputStream.read(tmp,0, 16))==16) {
          System.out.println(i);
          byte[] newResult = new byte[result.length + i];
          System.arraycopy(result, 0, newResult, 0, result.length);
          System.arraycopy(tmp, 0, newResult, result.length, i);
          result = newResult;
          System.out.println(new String(tmp));
        }

        if (i != -1) {
          System.out.println(i);
          byte[] newResult = new byte[result.length+i];
          System.arraycopy(result,0,newResult,0,result.length);
          System.arraycopy(tmp,0,newResult,result.length,i);
          result = newResult;
          System.out.println(new String(tmp));
        }
        System.out.println("hi");
        System.out.println(new String(result));

        int idx = 0;
        int l = (int)result[idx+1] & 0xFF;
        l += ((int)result[idx] & 0xFF) << 8;
        System.out.println(l);
        idx = idx + 2;
        while (l > 0) {
          int lenOfExpression = (int)result[idx+1] & 0xFF;
          lenOfExpression += ((int)result[idx] & 0xFF) << 8;
          idx += 2;
          byte[] expression = Arrays.copyOfRange(result,idx,idx+lenOfExpression);
          System.out.println(new String(expression));
          idx += lenOfExpression;
          l --;
        }
        /**
        int i = 0;

        while (i<numOfExpressions) {
          byte[] b = new byte[2];
          inputStream.read(b,0,2);
          Integer lenOfExpression = Integer.valueOf(Arrays.toString(b));
          byte[] expr = new byte[lenOfExpression];
          int j = 0;
          while (j < lenOfExpression / 16) {
            byte[] tmp = new byte[16];
            inputStream.read(tmp,0,16);
            j++;
          }
          byte[] remain = new byte[lenOfExpression % 16];
          inputStream.read(remain,0,lenOfExpression % 16);
          i ++;
        }
         **/
        //String line;
        //while ((line = reader.readLine()) != null) {
          //System.out.println("Recv: " + line);
          //writer.println(line);
        //}
        clientSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }
}
