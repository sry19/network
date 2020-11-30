package hw;

import static hw.Config.TIMEOUT_MSEC;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

//receiver
//bazel run //src/main/java/hw:file_receiver -- gbn ~/Downloads/cat11.jpg
//bazel run //src/main/java/hw:msg_receiver -- gbn

//sender
//bazel run //src/main/java/hw:file_sender -- gbn ~/Documents/5700hw/hw33/src/main/java/hw/grumpy_cat.jpg
//bazel run //src/main/java/hmsg_sender -- gbn


// TODO.
public class GoBackN extends TransportLayer {

  private int base;
  private int nextSeqNum;
  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private int expectedSeqNum;
  private Queue<byte[]> q;
  private int oldBase;

  public GoBackN(NetworkLayer networkLayer) {
    super(networkLayer);
    this.base = 1;
    this.nextSeqNum = 1;
    this.sem = new Semaphore(Config.WINDOW_SIZE);  // Guard to send 1 pkt at a time.
    scheduler = Executors.newScheduledThreadPool(1);
    this.expectedSeqNum = 1;
    this.q = new LinkedList<>();
    this.oldBase = 1;
  }

  @Override
  public void send(byte[] data) throws IOException {

    try {

      sem.acquire();

      //make a packet
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_DATA);
      byte[] bytesOfSeq = this.convertIntToBigEndian(this.nextSeqNum);
      int checkSum = 0;
      checkSum = checkSum + this.convertBigEndianToInt(typeOfData);
      checkSum = checkSum + this.convertBigEndianToInt(bytesOfSeq);
      for (int i = 0; i < data.length ; i=i+2) {
        byte[] slice = Arrays.copyOfRange(data, i, i+2);
        checkSum = checkSum + this.convertBigEndianToInt(slice);
      }
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      byte[] result = new byte[typeOfData.length+bytesOfSeq.length+byteOfChecksum.length+data.length];
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(bytesOfSeq,0,result,typeOfData.length,bytesOfSeq.length);
      System.arraycopy(byteOfChecksum,0,result,typeOfData.length+bytesOfSeq.length,byteOfChecksum.length);
      System.arraycopy(data,0,result,typeOfData.length+bytesOfSeq.length+byteOfChecksum.length,data.length);

      this.q.add(result);
      System.out.println(this.q);

      //send a packet
      byte[] copy = new byte[result.length];
      System.arraycopy(result,0,copy,0,result.length);
      networkLayer.send(copy);

      if (this.base == this.nextSeqNum) {
        System.out.println("start timer");
        timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(this.q),
            TIMEOUT_MSEC,
            TIMEOUT_MSEC,
            TimeUnit.MILLISECONDS);
      }
      this.nextSeqNum = this.nextSeqNum + 1;
      //timerSem.release();

      //sendDataSem.release();

      new Thread(() -> {
        try {
          int b = 1;
          do {
            System.out.println("in the while loop");
            byte[] msg = networkLayer.recv();
            System.out.println("ack"+Arrays.toString(msg));
            //System.out.println("ack" + Arrays.toString(msg));
            if (msg.length >= 6) {
              byte[] rtypeOfData = Arrays.copyOfRange(msg, 0, 2);
              byte[] rack = Arrays.copyOfRange(msg, 2, 4);
              byte[] rcheckSum = Arrays.copyOfRange(msg, 4, 6);
              if (this.convertBigEndianToInt(rtypeOfData) == Config.MSG_TYPE_ACK
                  && this.convertBigEndianToInt(rack) + this.convertBigEndianToInt(rtypeOfData)
                  == this.convertBigEndianToInt(rcheckSum) ) {

                this.base = this.convertBigEndianToInt(rack) + 1;
                //this.base >= this.nextSeqNum || this.convertBigEndianToInt(rack) + 1 >= this.nextSeqNum
                System.out.println("base="+this.base+"|nextseqnum="+this.nextSeqNum+"|ack="+this.convertBigEndianToInt(rack));
                //for (int i=0;i<this.base-oldbase;i++) {
                  //if (!q.isEmpty()) {
                    //this.q.remove();

                  //}

                //}
                while (this.oldBase < this.base) {
                  if (!this.q.isEmpty()) {
                    this.q.remove();
                    this.oldBase = this.oldBase + 1;
                  } else {
                    break;
                  }
                }
                if (this.base == this.nextSeqNum ) {
                  timer.cancel(true);
                  System.out.println("stop timer");
                  sem.release();
                  b = 0;
                } else {
                  System.out.println("restart timer");
                  timer.cancel(true);
                  timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(this.q),
                      TIMEOUT_MSEC,
                      TIMEOUT_MSEC,
                      TimeUnit.MILLISECONDS);

                }
                //timerSem.release();

                //sem.release();
                //b = 0;
              }
            }
          } while (b != 0);
          //timer.cancel(true);
          //sem.release();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();


    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Override
  public byte[] recv() throws IOException {

    byte[] data = networkLayer.recv();
    //System.out.println(Arrays.toString(data));
    if (data.length < 2) {
      return new byte[0];
    }
    byte[] typeOfData = Arrays.copyOfRange(data, 0, 2);
    if (this.convertBigEndianToInt(typeOfData) == Config.MSG_TYPE_DATA) {
      return this.dealWithData(data);
    } else {
      return new byte[0];
    }

  }

  public byte[] dealWithData(byte[] data) throws IOException {
    int checkSum;
    int seqNum;
    byte[] payload;
    if (data.length > 6) {

      byte[] typeOfData = Arrays.copyOfRange(data, 0, 2);
      byte[] byteOfSeq = Arrays.copyOfRange(data, 2, 4);
      byte[] byteOfCheckSum = Arrays.copyOfRange(data, 4, 6);
      payload = Arrays.copyOfRange(data, 6, data.length);

      checkSum = this.convertBigEndianToInt(byteOfCheckSum);
      checkSum = checkSum - this.convertBigEndianToInt(typeOfData);
      checkSum = checkSum - this.convertBigEndianToInt(byteOfSeq);

      for (int i = 0; i < payload.length; i = i + 2) {
        byte[] slice = Arrays.copyOfRange(payload, i, i + 2);
        checkSum = checkSum - this.convertBigEndianToInt(slice);
      }

      seqNum = this.convertBigEndianToInt(byteOfSeq);
    } else {
      return new byte[0];
    }
    byte[] result = new byte[6];
    //System.out.println("checksum==0:"+(checkSum+65536)%65536);
    //System.out.println("seqNum==expectedSeqNum:"+seqNum+"|"+this.expectedSeqNum);
    if ((checkSum+65536)%65536 == 0 && seqNum == this.expectedSeqNum) {
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_ACK);
      byte[] ack = this.convertIntToBigEndian(this.expectedSeqNum);
      int checksum = this.convertBigEndianToInt(typeOfData);
      checkSum = checksum + this.convertBigEndianToInt(ack);
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(ack, 0, result, typeOfData.length, ack.length);
      System.arraycopy(byteOfChecksum, 0, result, ack.length+typeOfData.length, byteOfChecksum.length);
      networkLayer.send(result);
      this.expectedSeqNum = this.expectedSeqNum + 1;
      return payload;
    } else  {
      byte[] typeOfData = this.convertIntToBigEndian(Config.MSG_TYPE_ACK);
      byte[] ack = this.convertIntToBigEndian(this.expectedSeqNum-1);
      int checksum = this.convertBigEndianToInt(typeOfData);
      checkSum = checksum + this.convertBigEndianToInt(ack);
      byte[] byteOfChecksum = this.convertIntToBigEndian(checkSum);
      System.arraycopy(typeOfData, 0, result, 0, typeOfData.length);
      System.arraycopy(ack, 0, result, typeOfData.length, ack.length);
      System.arraycopy(byteOfChecksum, 0, result, ack.length+typeOfData.length, byteOfChecksum.length);
      networkLayer.send(result);
      return new byte[0];
    }
  }

  private class RetransmissionTask implements Runnable {
    private Queue<byte[]> data;

    public RetransmissionTask(Queue<byte[]> data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        for (byte[] d: data) {
          byte[] copy = new byte[d.length];
          System.arraycopy(d, 0, copy, 0, d.length);
          networkLayer.send(copy);
        }

        //System.out.println("retransmit"+Arrays.toString(copy));

        //System.out.println("retransmit:"+Arrays.toString(this.data));
      } catch (Exception e) {
        e.printStackTrace();
        //System.out.println("retransmission bug");
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

  @Override
  public void close() throws IOException {
    System.out.println(this.q);
    //q.remove();
    System.out.println("avail size"+sem.availablePermits());
    System.out.println(this.oldBase);
    while (!this.q.isEmpty()) {
      //System.out.println("wait");
    }
    //timer.cancel(true);

    super.close();
    /**
    try {
      System.out.println(this.q);
      while (!this.q.isEmpty()) {
        //System.out.println("wait");
      }
      System.out.println("finally empty"+this.q);
      //timer.cancel(true);
      for (int i=0 ; i<Config.WINDOW_SIZE;i++) {
        //timer.cancel(true);
        sem.acquire();
        System.out.println(i);
      }
      timer.cancel(true);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.close();
    System.out.println("yyyclose");
    **/
  }
}
