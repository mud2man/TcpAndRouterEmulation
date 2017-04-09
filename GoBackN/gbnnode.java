import java.io.*;
import java.net.*;
import java.util.*;

public class gbnnode{
    public static void main(String args[]) throws Exception{
        int serverPort, clientPort, windowSize, valN;
        boolean isProb;
        double valP;
        ClientServer clientServer;
        
        clientPort = Integer.parseInt(args[0]);
        serverPort = Integer.parseInt(args[1]);
        windowSize = Integer.parseInt(args[2]);
        valN = 0;
        valP = 0;
        isProb = false;
         
        if(args[3].compareTo("-d") == 0){
            valN = Integer.parseInt(args[4]);
            isProb = false;
        }
        else if(args[3].compareTo("-p") == 0){
            valP = Double.parseDouble(args[4]);
            isProb = true;
        }
        else{
            System.err.println("Wrong usage");
        }

        //System.out.println("clientPort: " + clientPort);
        //System.out.println("serverPort: " + serverPort);
        //System.out.println("windowSize: " + windowSize);
        //System.out.println("valN: " + valN);
        //System.out.println("valP: " + valP);
        
        clientServer = new ClientServer(serverPort, clientPort, windowSize, valP, valN, isProb);
        clientServer.mainLoop();
    }
}
