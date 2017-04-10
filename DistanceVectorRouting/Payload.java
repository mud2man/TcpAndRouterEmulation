/*
 * payloadType0: ack => type, port, seqNum
 * payloadType1: data => type, port, seqNum, data
 * payloadType2: finish => type, port
 * payloadType3: table update => type, port, distance vector
 */

import java.util.*;

public class Payload{
    public int type;
    public int seqNum;
    public int port;
    public String data;
    HashMap<Integer, Double> distanceVector;

    Payload(){
        type = 0;
        data = "";
        seqNum = 0;
        port = 0;
        distanceVector = new HashMap<Integer, Double>();
    }
}
