/* 
 * Encode format: msg0Length.msg0 + msg1Length.msg1 + msg2Length.msg2 ...
 */

import java.util.*;

public class Serial{
    Serial(){
    }

    public String serialize(Payload payload){
        String msg, typeStr, portStr, data, seqNumStr, distStr;
        int port;
        double distance;
        
        msg = "";

        //encoe type
        typeStr = Integer.toString(payload.type);
        msg += Integer.toString(typeStr.length());
        msg += ".";
        msg += typeStr;
        
        switch (payload.type){
            case 0:
                //encode port
                portStr = Integer.toString(payload.port);
                msg += Integer.toString(portStr.length());
                msg += ".";
                msg += portStr;

                //encode seqNum
                seqNumStr = Integer.toString(payload.seqNum);
                msg += Integer.toString(seqNumStr.length());
                msg += ".";
                msg += seqNumStr;
                break;

            case 1:
                //encode port
                portStr = Integer.toString(payload.port);
                msg += Integer.toString(portStr.length());
                msg += ".";
                msg += portStr;

                //encode seqNum
                seqNumStr = Integer.toString(payload.seqNum);
                msg += Integer.toString(seqNumStr.length());
                msg += ".";
                msg += seqNumStr;

                //encode data
                msg += Integer.toString(payload.data.length());
                msg += ".";
                msg += payload.data;
                break;

            case 2:
                //encode port
                portStr = Integer.toString(payload.port);
                msg += Integer.toString(portStr.length());
                msg += ".";
                msg += portStr;

                distStr = Double.toString(payload.distance); 
                msg += Integer.toString(distStr.length());
                msg += ".";
                msg += distStr;
                break;

            case 3:
                //encode port
                portStr = Integer.toString(payload.port);
                msg += Integer.toString(portStr.length());
                msg += ".";
                msg += portStr;

                //encode distance vector
                for(Map.Entry<Integer, Double> entry : payload.distanceVector.entrySet()) {
    	            port = entry.getKey();
                    portStr = Integer.toString(port);
                    msg += Integer.toString(portStr.length());
                    msg += ".";
                    msg += portStr;
                    
                    distance = entry.getValue();
                    distStr = Double.toString(distance); 
                    msg += Integer.toString(distStr.length());
                    msg += ".";
                    msg += distStr;
                }    
                break;

            default:
                break;
        }
        //System.out.println("[Serial] msg:" + msg);
        return msg;
    }

    public Payload deserialize(String msg){
        int len;
        String subStr;
        int port;
        double distance;

        Payload payload = new Payload();
        
        //decode type
        subStr = msg.substring(0, msg.indexOf('.'));
        msg = msg.substring(msg.indexOf('.') + 1);
        len = Integer.parseInt(subStr);
        subStr = msg.substring(0, len);
        msg = msg.substring(len);
        payload.type = Integer.parseInt(subStr);

        //System.out.println("[Serial] type:" + payload.type);

        switch (payload.type){
            case 0:
                //decode port
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.port = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //decode seqNum
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.seqNum = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //System.out.println("[Serial] port:" + payload.port);
                //System.out.println("[Serial] seqNum:" + payload.seqNum);
                break;

            case 1:
                //decode port
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.port = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //decode seqNum
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.seqNum = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //decode data
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                payload.data = msg.substring(0, len);
                msg = msg.substring(len);
                
                //System.out.println("[Serial] port:" + payload.port);
                //System.out.println("[Serial] seqNum:" + payload.seqNum);
                //System.out.println("[Serial] data:" + payload.data);
                break;
            
            case 2:
                //decode port
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.port = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //decode distance
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.distance = Double.parseDouble(subStr);
                msg = msg.substring(len);

                //System.out.println("[Serial] port:" + payload.port);
                //System.out.println("[Serial] distance:" + payload.distance);
                break;
            
            case 3:
                //decode port
                subStr = msg.substring(0, msg.indexOf('.'));
                msg = msg.substring(msg.indexOf('.') + 1);
                len = Integer.parseInt(subStr);
                subStr = msg.substring(0, len);
                payload.port = Integer.parseInt(subStr);
                msg = msg.substring(len);

                //decode distance vector
                while(msg.indexOf('.') >= 0){
                    subStr = msg.substring(0, msg.indexOf('.'));
                    msg = msg.substring(msg.indexOf('.') + 1);
                    len = Integer.parseInt(subStr);
                    subStr = msg.substring(0, len);
                    port = Integer.parseInt(subStr);
                    msg = msg.substring(len);
                    
                    subStr = msg.substring(0, msg.indexOf('.'));
                    msg = msg.substring(msg.indexOf('.') + 1);
                    len = Integer.parseInt(subStr);
                    subStr = msg.substring(0, len);
                    distance = Double.parseDouble(subStr);
                    msg = msg.substring(len);
                    
                    payload.distanceVector.put(port, distance);
                }

                //System.out.println("[Serial] port:" + payload.port);
                //for(Map.Entry<Integer, Double> entry : payload.distanceVector.entrySet()) {
    	        //    port = entry.getKey();
                //    distance = entry.getValue();
                //    System.out.println("[Serial] <" + port + ", " + distance + ">");
                //} 
                break;

            default:
                break;
        }

        return payload;
    }
}
