import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientServer{
    int serverPort;
    boolean isLast;
    HashMap<Integer, Double> neighbors;
    HashMap<Integer, Double> distanceVector;
    HashMap<Integer, Integer> nextHops;
    HashMap<Integer, HashMap<Integer, Double>> neighborDistanceVectors;
    DatagramSocket socket;
    private Thread thread;
    Semaphore mutex;
    Date timeStamp;

    public ClientServer(int serverPort, HashMap<Integer, Double> neighbors, boolean isLast) throws Exception{
        int neighborPort;
        double distance;

        this.serverPort = serverPort;
        this.neighbors = neighbors;
        this.isLast = isLast; 
        this.socket = new DatagramSocket(this.serverPort);
        this.mutex = new Semaphore(1);
        this.distanceVector = new HashMap<Integer, Double>();
        this.nextHops = new HashMap<Integer, Integer>();
        this.neighborDistanceVectors = new HashMap<Integer, HashMap<Integer, Double>>();
        this.timeStamp = new Date();

        //System.out.println("[ClientServer] serverPort:" + this.serverPort);
        //System.out.println("[ClientServer] isLast:" + this.isLast);

        distanceVector.put(serverPort, 0.0);
        for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	    neighborPort = entry.getKey();
            distance = entry.getValue();
            distanceVector.put(neighborPort, distance);
            nextHops.put(neighborPort, neighborPort);
            //System.out.println("[ClientServer] <" + neighborPort + ", " + distance + ">");
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

            //System.out.println("[ClientServer] Running " +  threadName);
            serial = new Serial();
            
            for(Map.Entry<Integer, Double> entry : neighbors.entrySet()) {
    	        neighborPort = entry.getKey();
                payload = new Payload();
                payload.type = 3;
                payload.port = serverPort;
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
    
    public boolean routingTableUpdate(int neighborPort, HashMap<Integer, Double> neighborDistanceVector){
        boolean needUpdate;
        int destination, nextHop;
        double minDistance, currDistance;
    
        needUpdate = false;
        nextHop = 0;
        
        neighborDistanceVectors.put(neighborPort, neighborDistanceVector);
        
        //Bellman-Ford
        for(Map.Entry<Integer, Double> entry : neighborDistanceVector.entrySet()){
            destination = entry.getKey();

            if(destination == serverPort){
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
            System.out.println("[" + this.timeStamp.getTime() + "] " + "Node " + serverPort + " Routing Table");
            for(Map.Entry<Integer, Double> entry : distanceVector.entrySet()){
                if(entry.getKey() == serverPort){
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

        return needUpdate;
    }

    public void send(String msg, InetAddress ipAddress, int port) throws Exception{
        DatagramPacket sendPacket;
        
        //System.out.println("[ClientServer] send msg:" + msg);

        sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, ipAddress, port);
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

    private class ProcessorHook extends Thread {
        @Override
        public void run(){
        }
    }

    public void mainLoop() throws Exception{
        BroadcastThread workerThread;
        Payload recPayload;

        Runtime.getRuntime().addShutdownHook(new ProcessorHook());
        recPayload = null; 
        Thread.sleep(100);

        if(this.isLast){
            workerThread = new BroadcastThread("Broadcast vectors on node " + Integer.toString(this.serverPort));
            workerThread.start();
        }
        
        //listen for routing table upadte from neighbors
        while(true){
            try{
                //System.out.println("[ClientServer] wait for packet...");
                recPayload = receive();
            }
            catch(Exception e){
                e.printStackTrace();  
            }
            
            //System.out.println("[ClientServer] type:" + recPayload.type);
            switch (recPayload.type){
                case 0:
                    System.err.println("[ClientServer] wrong packet type:" + recPayload.type);
                    break;
                 
                case 1:
                    System.err.println("[ClientServer] wrong packet type:" + recPayload.type);
                    break;
                 
                case 2:
                    System.err.println("[ClientServer] wrong packet type:" + recPayload.type);
                    break;    
                
                case 3:
                    if(routingTableUpdate(recPayload.port, recPayload.distanceVector)){
                        workerThread = new BroadcastThread("Broadcast vectors on node " + Integer.toString(this.serverPort));
                        workerThread.start();
                    }
                    break;
   
                default:
                    break;
            }
        }
    }
}
