package hw;

import static hw.Config.TIMEOUT_MSEC;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO.
public class GoBackN extends TransportLayer {

  private Counter base;
  private Counter nextSeqNum;
  private Semaphore sem;
  private Semaphore sendDataSem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private Counter expectedSeqNum;

  public GoBackN(NetworkLayer networkLayer) {
    super(networkLayer);
    this.base = new Counter(1);
    this.nextSeqNum = new Counter(1);
    this.sem = new Semaphore(Config.WINDOW_SIZE);  // Guard to send 1 pkt at a time.
    this.sendDataSem = new Semaphore(1);
    scheduler = Executors.newScheduledThreadPool(1);
    this.expectedSeqNum = new Counter(1);
  }

  @Override
  public void send(byte[] data) throws IOException {
    /**
    //make a packet
    try {

      sem.acquire();
      sendDataSem.acquire();
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_DATA);
      byte[] bytesOfSeq = this.convertIntToBigEndian(this.nextSeqNum);

      int checkSum = 0;
      checkSum = checkSum + this.convertBigEndianToInt(typeOfData);
      checkSum = checkSum + this.convertBigEndianToInt(bytesOfSeq);

      for (int i = 0; i < data.length ; i=i+2) {
        byte[] slice = Arrays.copyOfRange(data, i, i+2);
        checkSum = checkSum + this.convertBigEndianToInt(slice);
      }
      System.out.println("checksum on sender side:"+checkSum);
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      byte[] result = new byte[typeOfData.length+bytesOfSeq.length+byteOfChecksum.length+data.length];
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(bytesOfSeq,0,result,typeOfData.length,bytesOfSeq.length);
      System.arraycopy(byteOfChecksum,0,result,typeOfData.length+bytesOfSeq.length,byteOfChecksum.length);
      System.arraycopy(data,0,result,typeOfData.length+bytesOfSeq.length+byteOfChecksum.length,data.length);
      System.out.println("whatIsend:"+Arrays.toString(result));

      //send a packet
      byte[] copy = new byte[result.length];
      System.arraycopy(result,0,copy,0,result.length);
      networkLayer.send(copy);

      sendDataSem.release();

      //flag = this.convertBigEndianToInt(bytesOfSeq);
      if (this.base == this.nextSeqNum) {
        timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(result),
            TIMEOUT_MSEC,
            TIMEOUT_MSEC,
            TimeUnit.MILLISECONDS);
        this.nextSeqNum = this.nextSeqNum + 1;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Start thread to read ACK.

    new Thread(() -> {
      try {
        int b = 1;
        do {
          //System.out.println("in the while loop");
          byte[] msg = networkLayer.recv();
          //System.out.println("ack" + Arrays.toString(msg));
          if (msg.length == 6) {
            byte[] typeOfData = Arrays.copyOfRange(msg, 0, 2);
            byte[] ack = Arrays.copyOfRange(msg, 2, 4);
            byte[] checkSum = Arrays.copyOfRange(msg, 4, 6);
            if (this.convertBigEndianToInt(typeOfData) == Config.MSG_TYPE_ACK
                && this.convertBigEndianToInt(ack) + this.convertBigEndianToInt(typeOfData)
                == this.convertBigEndianToInt(checkSum)) {
              //System.out.println("packet is received");
              this.base = this.convertBigEndianToInt(ack) + 1;
              if (this.base == this.nextSeqNum) {
                timer.cancel(true);
              }
              sem.release();
              b = 0;
            }
          }
        } while (b != 0);
        //timer.cancel(true);
        //sem.release();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
**/
  }

  @Override
  public byte[] recv() throws IOException {
    /**
    byte[] data = networkLayer.recv();
    //System.out.println(Arrays.toString(data));
    if (data.length < 2) {
      //String err = "data length < 2";
      return new byte[0];
    }
    byte[] typeOfData = Arrays.copyOfRange(data, 0, 2);
    if (this.convertBigEndianToInt(typeOfData) == Config.MSG_TYPE_DATA) {
      return this.dealWithData(data);
    } else {
      //String err = "not a data packet:"+this.convertBigEndianToInt(typeOfData);
      return new byte[0];
    }
     **/
     return null;
  }
/**
  public byte[] dealWithData(byte[] data) throws IOException {
    int checkSum;
    int seqNum;
    byte[] payload;
    if (data.length > 6) {
      System.out.println("receive data");
      byte[] typeOfData = Arrays.copyOfRange(data, 0, 2);
      byte[] byteOfSeq = Arrays.copyOfRange(data, 2, 4);
      byte[] byteOfCheckSum = Arrays.copyOfRange(data, 4, 6);
      payload = Arrays.copyOfRange(data, 6, data.length);

      checkSum = this.convertBigEndianToInt(byteOfCheckSum);
      System.out.println("cs on receiver side:"+checkSum);
      checkSum = checkSum - this.convertBigEndianToInt(typeOfData);
      checkSum = checkSum - this.convertBigEndianToInt(byteOfSeq);

      for (int i = 0; i < payload.length; i = i + 2) {
        byte[] slice = Arrays.copyOfRange(payload, i, i + 2);
        checkSum = checkSum - this.convertBigEndianToInt(slice);
      }

      seqNum = this.convertBigEndianToInt(byteOfSeq);
    } else {
      //System.out.println("data is corrupted");
      //String err = "error";
      //byte[] error = err.getBytes();
      return new byte[0];
    }
    byte[] result = new byte[6];
    System.out.println("checksum==0:"+checkSum);
    System.out.println("seqNum==recSeq:"+seqNum+"|"+this.expectedSeqNum);
    if (checkSum == 0 && seqNum == this.expectedSeqNum) {
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_ACK);
      byte[] ack = this.convertIntToBigEndian(seqNum);
      int checksum = this.convertBigEndianToInt(typeOfData);
      checkSum = checksum + this.convertBigEndianToInt(ack);
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(ack, 0, result, typeOfData.length, ack.length);
      System.arraycopy(byteOfChecksum, 0, result, ack.length+typeOfData.length, byteOfChecksum.length);
      networkLayer.send(result);
      this.expectedSeqNum = this.expectedSeqNum + 1;
      return payload;
    } else {
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_ACK);
      byte[] ack = this.convertIntToBigEndian(seqNum);
      int checksum = this.convertBigEndianToInt(typeOfData);
      checkSum = checksum + this.convertBigEndianToInt(ack);
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(ack, 0, result, typeOfData.length, ack.length);
      System.arraycopy(byteOfChecksum, 0, result, ack.length+typeOfData.length, byteOfChecksum.length);
      networkLayer.send(result);
      //String err = "duplicate";
      //byte[] error = err.getBytes();
      return new byte[0];
    }
  }
**/
  private class RetransmissionTask implements Runnable {
    private byte[] data;

    public RetransmissionTask(byte[] data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        byte[] copy = new byte[this.data.length];
        System.arraycopy(this.data,0,copy,0,this.data.length);
        networkLayer.send(copy);
        //System.out.println("retransmit:"+Arrays.toString(this.data));
      } catch (Exception e) {
        e.printStackTrace();
      }
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

  private static class Counter {
    private int i = 0;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock writeLock = lock.writeLock();

    public Counter(int input) {
      this.i = input;
    }

    public void inc() {
      writeLock.lock();
      ++i;
      writeLock.unlock();
    }

    public void dec() {
      writeLock.lock();
      --i;
      writeLock.unlock();
    }

    public int get() {
      return i;
    }
  }
}
