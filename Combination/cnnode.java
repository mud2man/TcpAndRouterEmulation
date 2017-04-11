import java.io.*;
import java.net.*;
import java.util.*;

public class cnnode{
    public static void main(String args[]) throws Exception{
        int serverPort, neighborPort, idx;
        double distance;
        HashMap<Integer, Double> neighbors;
        HashSet<Integer> probeSnedees;
        ClientServer clientServer;
        boolean isLast;
        
        isLast = false;
        serverPort = Integer.parseInt(args[0]);
        neighbors = new HashMap<Integer, Double>();
        probeSnedees = new HashSet<Integer>();

        //parse receiver
        idx = 2;
        while(!args[idx].equals("send")){
            neighborPort = Integer.parseInt(args[idx++]);
            distance = Double.parseDouble(args[idx++]);
            neighbors.put(neighborPort, distance);
        }

        //parse sneder
        idx++;
        while(idx < args.length){
            if(args[idx].equals("last")){
                isLast = true;
                break;
            }
            neighborPort = Integer.parseInt(args[idx++]);
            distance = 0;
            neighbors.put(neighborPort, distance);
            probeSnedees.add(neighborPort);
        }
        
        clientServer = new ClientServer(serverPort, neighbors, isLast, probeSnedees);
        clientServer.mainLoop();
    }
}
