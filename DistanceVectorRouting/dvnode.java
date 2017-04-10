import java.io.*;
import java.net.*;
import java.util.*;

public class dvnode{
    public static void main(String args[]) throws Exception{
        int serverPort, neighborPort, idx;
        double distance;
        HashMap<Integer, Double> neighbors;
        ClientServer clientServer;
        boolean isLast;
        
        serverPort = Integer.parseInt(args[0]);
        neighbors = new HashMap<Integer, Double>();

        if(args.length % 2 == 0){
            isLast = true;
        }
        else{
            isLast = false;
        }
        
        idx = 1;
        if(isLast){
            while(idx < args.length - 1){
                neighborPort = Integer.parseInt(args[idx++]);
                distance = Double.parseDouble(args[idx++]);
                neighbors.put(neighborPort, distance);
            }
        }
        else{
            while(idx < args.length){
                neighborPort = Integer.parseInt(args[idx++]);
                distance = Double.parseDouble(args[idx++]);
                neighbors.put(neighborPort, distance);
            }
        }

        clientServer = new ClientServer(serverPort, neighbors, isLast);
        clientServer.mainLoop();
    }
}
