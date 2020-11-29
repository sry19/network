package hw;

import java.io.IOException;
import java.util.Arrays;

// DummyTransportLayer is an example of transport layer that doesn't
// deal with bit error or data loss.
public class DummyTransportLayer extends TransportLayer {
  public DummyTransportLayer(NetworkLayer networkLayer) {
    super(networkLayer);
  }

  @Override
  public void send(byte[] data) throws IOException {
    networkLayer.send(data);
  }

  @Override
  public byte[] recv() throws IOException {
    byte[] bytesOfSeq = new byte[2];



    System.out.println(new String(Arrays.copyOf(bytesOfSeq, 0)));
    return networkLayer.recv();
  }
}
