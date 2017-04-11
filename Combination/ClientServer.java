import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientServer{
    int selfPort, sendCount, ackCount, windowSize, windowStart, windowEnd, ackNum;
    boolean isLast;
    HashMap<Integer, Double> neighbors;
    HashMap<Integer, Double> distanceVector;
    HashMap<Integer, Integer> nextHops;
    HashMap<Integer, HashMap<Integer, Double>> neighborDistanceVectors;
    HashSet<Integer> probeSnedees;
    HashMap<Integer, Integer> expectPkts;
    DatagramSocket socket;
    private Thread gThread;
    private Thread thread;
    Semaphore mutex;
    Date timeStamp;
    boolean isCaculated;
    boolean isNeighborsUpdate;

    public ClientServer(int selfPort, HashMap<Integer, Double> neighbors, boolean isLast, HashSet<Integer> probeSnedees) 
    throws Exception{
        int neighborPort;
        double distance;

        this.selfPort = selfPort;
        this.neighbors = neighbors;
        this.isLast = isLast;
        this.probeSnedees = probeSnedees;
        this.socket = new DatagramSocket(this.selfPort);
        this.mutex = new Semaphore(1);
        this.distanceVector = new HashMap<Integer, Double>();
        this.nextHops = new HashMap<Integer, Integer>();
        this.neighborDistanceVectors = new HashMap<Integer, HashMap<Integer, Double>>();
        this.isCaculated = false;
        this.windowSize = 5;
        this.expectPkts = new HashMap<Integer, Integer>();
        this.isNeighborsUpdate = false;
        //gThread = new Thread (this, "XXXX");

        System.out.println("[ClientServer] selfPort:" + this.selfPort);
        System.out.println("[ClientServer] isLast:" + this.isLast);
        System.out.println("[ClientServer] neighbors:");
        distanceVector.put(selfPort, 0.0);
        for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	    neighborPort = entry.getKey();
            distance = entry.getValue();
            distanceVector.put(neighborPort, distance);
            nextHops.put(neighborPort, neighborPort);
            expectPkts.put(neighborPort, 0);
            System.out.println("[ClientServer] <" + neighborPort + ", " + distance + ">");
        } 
        
        System.out.println("[ClientServer] probeSnedees:");
        for(Integer p : probeSnedees) {
            System.out.println("[ClientServer] " + p);
        }
    }
    
    public void routingTableReset(){
        int neighborPort;
        double distance;

        try{
            mutex.acquire();
        }
        catch(Exception e){
            e.printStackTrace();  
        }
        distanceVector = new HashMap<Integer, Double>();
        nextHops = new HashMap<Integer, Integer>();
        neighborDistanceVectors = new HashMap<Integer, HashMap<Integer, Double>>();
        
        distanceVector.put(selfPort, 0.0);
        for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	    neighborPort = entry.getKey();
            distance = entry.getValue();
            distanceVector.put(neighborPort, distance);
            nextHops.put(neighborPort, neighborPort);
        } 
        mutex.release();
    }
    
    public void dump(){
        HashMap<Integer, Double> tmpDistanceVector;
        int neighborPort;
        double distance;

        System.out.println("[dump] neighbors:");
        for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	    neighborPort = entry.getKey();
            distance = entry.getValue();
            System.out.println("<" + neighborPort + ", " + distance + ">");
        }
        
        System.out.println("[dump] distanceVector:");
        for(Map.Entry<Integer, Double> entry : distanceVector.entrySet()) {
    	    neighborPort = entry.getKey();
            distance = entry.getValue();
            System.out.println("<" + neighborPort + ", " + distance + ">");
        }
        
        System.out.println("[dump] neighborDistanceVectors:");
        for(Map.Entry<Integer, HashMap<Integer, Double>> neighborDistanceVector: neighborDistanceVectors.entrySet()) {
            System.out.println("  [dump] neighbor: " + neighborDistanceVector.getKey());
            tmpDistanceVector = neighborDistanceVector.getValue();
            for(Map.Entry<Integer, Double> entry : tmpDistanceVector.entrySet()) {
    	        neighborPort = entry.getKey();
                distance = entry.getValue();
                System.out.println("  <" + neighborPort + ", " + distance + ">");
            }
        }
    }
 
    public boolean isDrop(int neighborPort){
        double random;
        
        random = Math.random();
        if(random < neighbors.get(neighborPort)){
            //System.out.println("[ClientServer] drop...");
            return true;
        }
        else{
            return false;
        }
    }

    private class BroadcastThread implements Runnable {
        private String threadName;
        int neighborPort;
        String serialMsg;
       
        BroadcastThread(String name){
            threadName = name;
            thread = new Thread (this, threadName);
        }
       
        public void run() {
            InetAddress ipAddress;
            Payload sendPayload;
            String msg;
            Serial serial;
            Payload payload;

            System.out.println("[ClientServer] Running " +  threadName);
            serial = new Serial();
            
            for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	        neighborPort = entry.getKey();
                payload = new Payload();
                payload.type = 3;
                payload.port = selfPort;
                payload.distanceVector = distanceVector; 
                serialMsg = serial.serialize(payload);
                try{
                    ipAddress = InetAddress.getByName("localhost");
                    send(serialMsg, ipAddress, neighborPort);
                }
                catch(Exception e){
                    e.printStackTrace();  
                }
            } 
        }
       
        public void start () {
            thread.start ();
        }
    }
    
    private class ProbeThread implements Runnable {
        private String threadName;
        int neighborPort;
        String serialMsg;
       
        ProbeThread(String name){
            threadName = name;
            thread = new Thread (this, threadName);
        }
       
        public void tcpSend(String msg, int port) throws Exception{
            InetAddress ipAddress;
            Payload payload;
            Serial serial;
            int currPos, idx;
            long prevTime;
            double lossRate;
            String serialMsg;
 
            sendCount = 0;
            ackCount = 0;
            ackNum = 0;
            windowStart = 0;
            windowEnd = windowStart + windowSize - 1;
            prevTime = System.currentTimeMillis();
            currPos = 0;
            serial = new Serial();
            
            while(true){
                // transmition finish
                //System.out.println("this.windowStart:" + this.windowStart + ", msg.length():" + msg.length());
                mutex.acquire();
 
                //send finished packet to neighbor
                if(windowStart >= msg.length()){
                    payload = new Payload();
                    payload.type = 2;
                    payload.port = selfPort;
                    lossRate = (double)(sendCount - ackCount)/(double)(sendCount);
                    lossRate = Math.round (lossRate * 100.0) / 100.0;
                    neighbors.put(port, lossRate);
                    isNeighborsUpdate = true;
                    payload.distance = lossRate;
                    serialMsg = serial.serialize(payload);
                    ipAddress = InetAddress.getByName("localhost");
                    send(serialMsg, ipAddress, port);
                    mutex.release();
                    break;
                }
                
                //trasmit all packets in the window
                if((System.currentTimeMillis() - prevTime) >= 500){
                    idx = windowStart;
                    timeStamp = new Date();
                    //System.out.print("[" + timeStamp.getTime() + "] ");
                    //System.out.println("packet" + idx + " timeout");
                }
                else{
                    idx = currPos;
                }
                
                for(idx = idx; (idx <= windowEnd) && (idx < msg.length()); idx++){
                    payload = new Payload();
                    payload.type = 1;
                    payload.port = selfPort;
                    payload.seqNum = idx;
                    payload.data = msg.substring(idx, idx + 1);
                    //System.out.println("msg::::::" + msg);
                    //System.out.println("payload.data:::::" + payload.data);
                    timeStamp = new Date();
                    //System.out.print("[" + timeStamp.getTime() + "] ");
                    //System.out.println("packet" + payload.seqNum + " " + payload.data + " sent");
                    serialMsg = serial.serialize(payload);
                    ipAddress = InetAddress.getByName("localhost");
                    sendCount++;
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
            System.out.print("[Summery] " + (sendCount - ackCount) + "/" + sendCount);
            System.out.println(" packets discarded, loss rate = " + (lossRate * 100) + "%");
        }

        public void run() {
            isCaculated = true;
            
            //probedistance
            for(Integer port : probeSnedees) {
                try{
                    tcpSend("probe", port);
                }
                catch(Exception e){
                    e.printStackTrace();  
                }
            }
        }
       
        public void start () {
            thread.start ();
        }
    }
    
    public boolean routingTableUpdate(int neighborPort, HashMap<Integer, Double> neighborDistanceVector){
        boolean needUpdate;
        int destination, nextHop;
        double minDistance, currDistance;
    
        needUpdate = false;
        nextHop = 0;
        
        try{
            mutex.acquire();
        }
        catch(Exception e){
            e.printStackTrace();  
        }
        if(neighborPort != selfPort){  
            neighborDistanceVectors.put(neighborPort, neighborDistanceVector);
        }
        
        //Bellman-Ford
        for(Map.Entry<Integer, Double> entry : neighborDistanceVector.entrySet()){
            destination = entry.getKey();

            if(destination == selfPort){
                continue;
            }

            minDistance = Double.MAX_VALUE;
            //scan all the neighbors to get minimum distance
            for(Map.Entry<Integer, Double> neighbor : neighbors.entrySet()){
                neighborPort = neighbor.getKey();
                if(neighborDistanceVectors.containsKey(neighborPort) && 
                   neighborDistanceVectors.get(neighborPort).containsKey(destination)){
                    currDistance = neighbors.get(neighborPort) + neighborDistanceVectors.get(neighborPort).get(destination);
                    currDistance = Math.round (currDistance * 100.0) / 100.0;
                    if(currDistance < minDistance){
                        minDistance = currDistance;
                        nextHop = neighborPort;
                    }
                }
            }
            
            if(!distanceVector.containsKey(destination) || distanceVector.get(destination) != minDistance){
                distanceVector.put(destination, minDistance);
                nextHops.put(destination, nextHop);
                needUpdate = true;
            }
        }
       
        //display the routing table 
        if(needUpdate == true){
            timeStamp = new Date();
            System.out.print("[" + timeStamp.getTime() + "] ");
            System.out.println("Node " + selfPort + " Routing Table");
            for(Map.Entry<Integer, Double> entry : distanceVector.entrySet()){
                if(entry.getKey() == selfPort){
                    continue;
                }

                System.out.print("(" + entry.getValue() + ")" + " -> Node " + entry.getKey());

                if(nextHops.containsKey(entry.getKey()) && (entry.getKey() - nextHops.get(entry.getKey()) != 0)){
                    System.out.println("; Next hop -> Node " + nextHops.get(entry.getKey()));
                }
                else{
                    System.out.println("");
                }
            }
        }
        mutex.release();

        return needUpdate;
    }

    public void send(String msg, InetAddress ipAddress, int port) throws Exception{
        DatagramPacket sendPacket;
        
        //System.out.println("[ClientServer] send msg:" + msg);
        sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ipAddress, port);
        socket.send(sendPacket);
    }
    
    private class UpdateThread implements Runnable {
        private String threadName;
        int neighborPort;
        String serialMsg;
        double distance;
        BroadcastThread broadcastThread;
       
        UpdateThread(String name){
            threadName = name;
            thread = new Thread (this, threadName);
        }
       
        public void run() {
            InetAddress ipAddress;
            Payload sendPayload;
            String msg;
            Serial serial;
            Payload payload;

            System.out.println("[ClientServer] Running " +  threadName);
            serial = new Serial();
            
            //while(true){
                //wait for 5 seconds
                timeStamp = new Date();
                System.out.print("[" + timeStamp.getTime() + "] ");
                System.out.println("[ClientServer] wait for 5 sec...");
                try {
                    Thread.sleep(10000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                
                timeStamp = new Date();
                System.out.print("[" + timeStamp.getTime() + "] ");
                System.out.println("[ClientServer] wait for 5 sec complete..............");
                
                dump();
                routingTableReset();
                if(isNeighborsUpdate){
                    broadcastThread = new BroadcastThread("Broadcast vectors on node " + Integer.toString(selfPort));
                    broadcastThread.start();
                    isNeighborsUpdate = false;
                }
            //}
        }
       
        public void start () {
            thread.start ();
        }
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
        System.out.println("[ClientServer] receive msg:" + msg);
        payload = serial.deserialize(msg);

        return payload;
    }

    private class ProcessorHook extends Thread {
        @Override
        public void run(){
        }
    }

    public void mainLoop() throws Exception{
        BroadcastThread broadcastThread;
        ProbeThread probeThread;
        UpdateThread updateThread;
        Payload recPayload, sendPayload;
        boolean needWaken;
        String msg;
        Serial serial;
        InetAddress ipAddress;
        int neighborPort;
        double distance;

        Runtime.getRuntime().addShutdownHook(new ProcessorHook());
        recPayload = null; 
        Thread.sleep(100);
        serial = new Serial();

        if(this.isLast){
            broadcastThread = new BroadcastThread("Broadcast vectors on node " + Integer.toString(this.selfPort));
            broadcastThread.start();
        }
        
        //listen for routing table upadte from neighbors
        while(true){
            needWaken = false;

            try{
                System.out.println("[ClientServer] wait for packet...");
                recPayload = receive();
            }
            catch(Exception e){
                e.printStackTrace();  
            }
            
            System.out.println("[ClientServer] type:" + recPayload.type);
            switch (recPayload.type){
                case 0:
                    //update ackNum, window info
                    try{
                        synchronized(thread){
                            //System.out.println("[ClientServer] recPayload.seqNum: " + recPayload.seqNum);
                            //System.out.println("[ClientServer] ackNum: " + ackNum);

                            mutex.acquire();
                            if(recPayload.seqNum > ackNum){
                                ackNum = recPayload.seqNum;
                                windowStart = recPayload.seqNum + 1;
                                needWaken = true;
                            }
                            windowEnd = windowStart + windowSize - 1;
                            ackCount++;
                            mutex.release();

                            //timeStamp = new Date();
                            //System.out.print("[" + timeStamp.getTime() + "] ");
                            //System.out.println("ACK" + recPayload.seqNum + " received, window moves to " + windowStart);
                            
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
                    if(isDrop(recPayload.port)){
                        timeStamp = new Date();
                        //System.out.print("[" + timeStamp.getTime() + "] ");
                        //System.out.println("packet" + recPayload.seqNum + " " + recPayload.data + " discarded");
                        break;
                    }

                    if(recPayload.seqNum == expectPkts.get(recPayload.port)){
                        expectPkts.put(recPayload.port, expectPkts.get(recPayload.port) + 1);
                    }

                    timeStamp = new Date();
                    //System.out.print("[" + timeStamp.getTime() + "] ");
                    //System.out.println("packet" + recPayload.seqNum + " " + recPayload.data + " received");
                    sendPayload = new Payload();
                    sendPayload.type = 0;
                    sendPayload.port = selfPort;
                    sendPayload.seqNum = expectPkts.get(recPayload.port) - 1;
                    msg = serial.serialize(sendPayload);
                     
                    try{
                        ipAddress = InetAddress.getByName("localhost");
                        timeStamp = new Date();
                        //System.out.print("[" + timeStamp.getTime() + "] ");
                        //System.out.println("ACK" + sendPayload.seqNum + " sent, expecting packet" + expectPkts.get(recPayload.port));
                        send(msg, ipAddress, recPayload.port);
                    }
                    catch(Exception e){
                        e.printStackTrace();  
                    }
                    
                    break;
                 
                case 2:
                    //receive distance caculated by probe sender
    	            neighborPort = recPayload.port;
                    distance = recPayload.distance;
                    neighbors.put(neighborPort, distance);
                    isNeighborsUpdate = true;
                    break;    
                
                case 3:
                    //receive distance vector
                    if(routingTableUpdate(recPayload.port, recPayload.distanceVector)){
                        broadcastThread = new BroadcastThread("Broadcast vectors on node " + Integer.toString(this.selfPort));
                        broadcastThread.start();
                    }
                    
                    if(!isCaculated){
                        probeThread = new ProbeThread("Probe distance on node " + Integer.toString(this.selfPort));
                        probeThread.start();
                        
                        updateThread = new UpdateThread("Update distance vector on node " + Integer.toString(this.selfPort));
                        updateThread.start();
                    }
                    break;
   
                default:
                    break;
            }
        }
    }
}
