/*
 * payloadType0: ack => type, port, seqNum
 * payloadType1: data => type, port, seqNum, data
 * payloadType2: finish => type, port
 */

import java.util.*;

public class Payload{
    public int type;
    public int seqNum;
    public int port;
    public String data;

    Payload(){
        type = 0;
        data = "";
        seqNum = 0;
        port = 0;
    }
}
