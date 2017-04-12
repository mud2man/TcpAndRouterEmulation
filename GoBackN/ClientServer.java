import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientServer{
    boolean isProb, isSend;
    double valP;
    int windowSize, valN, serverPort, clientPort, windowStart, windowEnd, ackNum, expectPkt, recvAckCount, discardAckCount;
    DatagramSocket socket;
    private Thread thread;
    Date timeStamp;
    Semaphore mutex;

    public ClientServer(int serverPort, int clientPort, int windowSize, double valP, int valN, boolean isProb) throws Exception{
        this.serverPort = serverPort;
        this.clientPort = clientPort;
        this.windowSize = windowSize;
        this.valP = valP;
        this.valN = valN;
        this.isProb = isProb;
        this.isSend = false; 
        this.socket = new DatagramSocket(this.serverPort);
        this.mutex = new Semaphore(1);
        this.timeStamp = new Date();

        //System.out.println("[ClientServer] serverPort:" + this.serverPort);
        //System.out.println("[ClientServer] clientPort:" + this.clientPort);
        //System.out.println("[ClientServer] windowSize:" + this.windowSize);
        //System.out.println("[ClientServer] valP:" + this.valP);
        //System.out.println("[ClientServer] valN:" + this.valN);
        //System.out.println("[ClientServer] isProb:" + this.isProb);
        //System.out.println("[ClientServer] isSend:" + this.isSend);
    }
    
    public boolean isDrop(int recvCount){
        double random;
        
        if(valP > 0 && valN == 0){
            random = Math.random();
            if(random < valP){
                //System.out.println("[ClientServer] drop...");
                return true;
            }
            else{
                return false;
            }
        }
        else if (valP == 0 && valN > 0){
            if(recvCount % valN == valN - 1){
                //System.out.println("[ClientServer] drop...");
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
    
    private class ReceiveThread implements Runnable {
        private String threadName;
       
        ReceiveThread(String name){
            threadName = name;
            thread = new Thread (this, threadName);
        }
       
        public void run() {
            InetAddress ipAddress;
            Payload recPayload, sendPayload;
            String msg;
            Serial serial;
            boolean needWaken;
            Payload payload;
            int recvCount, dropCount;
            double lossRate;

            //System.out.println("[ClientServer] Running " +  threadName + " on port:"+ serverPort);
            serial = new Serial();
            recPayload = null;
            recvCount = -1;
            dropCount = 0;
            expectPkt = 0;

            while(true){
                needWaken = false;

                try{
                    //System.out.println("[ClientServer] wait for packet...");
                    recPayload = receive();
                    mutex.acquire();
                }
                catch(Exception e){
                    e.printStackTrace();  
                }
                
                //System.out.println("[ClientServer] type:" + recPayload.type);
                switch (recPayload.type){
                    case 0:
                        //update ackNum, window info
                        try{
                            synchronized(thread){
                                //System.out.println("[ClientServer] recPayload.seqNum: " + recPayload.seqNum);
                                //System.out.println("[ClientServer] ackNum: " + ackNum);
                                if(isDrop(++recvAckCount)){
                                    discardAckCount++;
                                    timeStamp = new Date();
                                    System.out.print("[" + timeStamp.getTime() + "] ");
                                    System.out.println("ACK" + recPayload.seqNum + " discarded");
                                    break;
                                }
                                if(recPayload.seqNum > ackNum){
                                    ackNum = recPayload.seqNum;
                                    windowStart = recPayload.seqNum + 1;
                                    needWaken = true;
                                }
                                windowEnd = windowStart + windowSize - 1;
                                timeStamp = new Date();
                                System.out.print("[" + timeStamp.getTime() + "] ");
                                System.out.println("ACK" + recPayload.seqNum + " received, window moves to " + windowStart);
                                
                                //System.out.println("[ClientServer] waken...");
                                if(needWaken){
                                    thread.notifyAll();
                                }
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();  
                        }
                        break;
                     
                    case 1:
                        //send ack# and update expecting packet#
                        if(isDrop(++recvCount)){
                            timeStamp = new Date();
                            System.out.print("[" + timeStamp.getTime() + "] ");
                            System.out.println("packet" + recPayload.seqNum + " " + recPayload.data + " discarded");
                            dropCount++;
                            break;
                        }

                        if(recPayload.seqNum == expectPkt){
                            expectPkt++;
                        }

                        timeStamp = new Date();
                        System.out.print("[" + timeStamp.getTime() + "] ");
                        System.out.println("packet" + recPayload.seqNum + " " + recPayload.data + " received");
                        payload = new Payload();
                        payload.type = 0;
                        payload.port = serverPort;
                        payload.seqNum = expectPkt - 1;
                        msg = serial.serialize(payload);
                        
                        //invalid case 
                        //if(payload.seqNum < 0){
                        //    break;
                        //}
                         
                        try{
                            ipAddress = InetAddress.getByName("localhost");
                            timeStamp = new Date();
                            System.out.print("[" + timeStamp.getTime() + "] ");
                            System.out.println("ACK" + payload.seqNum + " sent, expecting packet" + expectPkt);
                            send(msg, ipAddress, recPayload.port);
                        }
                        catch(Exception e){
                            e.printStackTrace();  
                        }
                        
                        break;
 
                    case 2:
                        //display loss rate and reset
                        expectPkt = 0;
                        ++recvCount;
                        lossRate = (double)dropCount/(double)recvCount;
                        lossRate = Math.round (lossRate * 1000.0) / 1000.0;
                        System.out.print("[Summery] " + dropCount + "/" + recvCount);
                        System.out.println(" packets dropped, loss rate = " + lossRate);
                        break;    
                    
                    default:
                        break;
                }
                mutex.release();
            }
        }
       
        public void start () {
            thread.start ();
        }
    }

    public void send(String msg, InetAddress ipAddress, int port) throws Exception{
        DatagramPacket sendPacket;
        
        sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ipAddress, port);
        //System.out.println("[ClientServer] send msg:" + msg);
        socket.send(sendPacket);
    }
    
    public Payload receive() throws Exception{
        String msg;
        byte[] receiveData;
        Payload payload;
        DatagramPacket receivePacket;
        Serial serial;
       
        serial = new Serial();
        receiveData = new byte[1024];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        msg = new String(receivePacket.getData());
        //System.out.println("[ClientServer] receive msg:" + msg);
        payload = serial.deserialize(msg);
        
        return payload;
    }
    
    public void tcpSend(String msg, int port) throws Exception{
        InetAddress ipAddress;
        Payload payload;
        Serial serial;
        int currPos, idx;
        long prevTime;
        double lossRate;
        String serialMsg;

        this.recvAckCount = 0;
        this.discardAckCount = 0;
        this.windowStart = 0;
        this.windowEnd = this.windowStart + windowSize - 1;
        prevTime = System.currentTimeMillis();
        currPos = 0;
        serial = new Serial();
        
        while(true){
            // transmition finish
            //System.out.println("this.windowStart:" + this.windowStart + ", msg.length():" + msg.length());
            mutex.acquire();

            //send finished packet to server
            if(this.windowStart >= msg.length()){
                payload = new Payload();
                payload.type = 2;
                payload.port = this.serverPort;
                serialMsg = serial.serialize(payload);
                ipAddress = InetAddress.getByName("localhost");
                send(serialMsg, ipAddress, port);
                mutex.release();
                break;
            }
            
            //trasmit all packets in the window
            if((System.currentTimeMillis() - prevTime) >= 500){
                idx = this.windowStart;
                timeStamp = new Date();
                System.out.print("[" + timeStamp.getTime() + "] ");
                System.out.println("packet" + idx + " timeout");
            }
            else{
                idx = currPos;
            }
            
            for(idx = idx; (idx <= this.windowEnd) && (idx < msg.length()); idx++){
                payload = new Payload();
                payload.type = 1;
                payload.port = this.serverPort;
                payload.seqNum = idx;
                payload.data = msg.substring(idx, idx + 1);
                //System.out.println("msg::::::" + msg);
                //System.out.println("payload.data:::::" + payload.data);
                timeStamp = new Date();
                System.out.print("[" + timeStamp.getTime() + "] ");
                System.out.println("packet" + payload.seqNum + " " + payload.data + " sent");
                serialMsg = serial.serialize(payload);
                ipAddress = InetAddress.getByName("localhost");
                send(serialMsg, ipAddress, port);
            }
            mutex.release();

            currPos = idx;
            
            //wait for timeout or window moved
            synchronized(thread){
                prevTime = System.currentTimeMillis();
                thread.wait(500);
            } 
        }
        
        //display loss rate
        lossRate =  (double)discardAckCount/(double)recvAckCount;
        lossRate = Math.round (lossRate * 1000.0) / 1000.0;
        System.out.print("[Summery] " + discardAckCount + "/" + recvAckCount);
        System.out.println(" packets discarded, loss rate = " + lossRate);
        this.recvAckCount = 0;
        this.discardAckCount = 0;
    }

    private class ProcessorHook extends Thread {
        @Override
        public void run(){
        }
    }

    public void mainLoop() throws Exception{
        ReceiveThread receiveThread;
        BufferedReader br;
        String command, strLine, msg; 

        Runtime.getRuntime().addShutdownHook(new ProcessorHook()); 
        receiveThread = new ReceiveThread("Receive thread");
        receiveThread.start();
        Thread.sleep(100); 
        
        while(true){
            System.out.print("node> ");
            br = new BufferedReader(new InputStreamReader(System.in));
            strLine = br.readLine();
            isSend = true;

            if(strLine.indexOf(' ') == -1){
                System.err.println("Wrong format !!!");
                continue; 
            }

            command = strLine.substring(0, strLine.indexOf(' '));
            strLine = strLine.substring(strLine.indexOf(' ') + 1);
            
            if(command.compareTo("send") == 0){
                msg = strLine.substring(strLine.indexOf(' ') + 1);
                this.ackNum = -1;
                tcpSend(msg, this.clientPort);
            }
            else{
                System.err.println("Wrong format !!!");
            }
        }
    }
}
