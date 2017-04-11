Name: Chih-Hung.Lu 
UNI: cl3519

I seperated this assignment as three parts and put them into three folders, GobackN, DistanceVectorRouting and Combination  

1. How to compile
    1.a For Go-Back-N Protocol, go into "GoBackN" folder do "$: make"
    1.b For Distance-Vector Routing Algorithm , go into "DistanceVectorRouting" folder do "$: make"
    1.c For Combination part , go into "Combination" folder do "$: make"

2. How to run
    2.a Go-Back-N Protocol:
        2.a.a sender: "$: java gbnnode 1111 2222 5 -p 0.5 (probabilistic mode)", or "$: java gbnnode 1111 2222 5 -p 0.5 (deterministic mode)", and "send <message>"
        2.a.b receiver: do nothing
    2.b Distance-Vector Routing Algorithm:
        2.b.a node 1111: "$: java dvnode 1111 2222 .1 3333 .5" or "$: sh 1111_test.sh"
        2.b.b node 2222: "$: java dvnode 2222 1111 .1 3333 .2 4444 .8" or "$: sh 2222_test.sh"
        2.b.c node 3333: "$: java dvnode 3333 1111 .5 2222 .2 4444 .5" or "$: sh 3333_test.sh"
        2.b.d node 4444: "$: java dvnode 4444 2222 .8 3333 .5 last" or "$: sh 4444_test.sh"
    2.c Combination:
        2.c.a node 1111: "$: java cnnode 1111 receive send 2222 3333" or "$: sh 1111_test.sh" 
        2.c.b node 2222: "$: java cnnode 2222 receive 1111 .1 send 3333 4444" or "$: sh 2222_test.sh"
        2.c.c node 3333: "$: java cnnode 3333 receive 1111 .5 2222 .2 send 4444" or "$: sh 3333_test.sh"
        2.c.d node 4444: "$: java cnnode 4444 receive 2222 .8 3333 .5 send last" or "$: sh 4444_test.sh"

3. Algorithms and datas tructure
    3.a Serialize and deserilize
        I use an efficeint way to serialize the message. The serialization format is that character# of message1 + 
        "." + message1 + character# of message2 + "." + message.... 

    3.b Neighbor information
        Hashmap are used to store distance vector, next hop information and neighbor's distance vector . The key is the port of neighbor and the 
        value of table is the object of needed information. Of course, the needed information of client is encapsulate into an object.

    3.c Multi-threading
        Worker thread are created to serve parallel service. Wait(timeout) and notifyAll are used to archieve the handshake betweein sneding and 
        receiving thread. Also, syncronized method are used to make atomic operation in critical region.

    3.d Protocals
        The following is the self-defined message type used to communicate among nodes.
       
        Type0: ack => type, port, seqNum
        Type1: data => type, port, seqNum, data
        Type2: finish => type, port, distance
        Type3: table update => type, port, distance vector

4. Test plan
    I have shell scripts for each part which can make testing in the same machine more convenient.
   
    4.a Go-Back-N Protocol
        4.a.a run "client_test.sh" in terminal#1
        4.a.b "server_test.sh" in terminal#2
        4.a.c In terminal#1, type "send abcd"

    4.b Go-Back-N Protocol testing result
        4.b.a terminal#1:
            namo@Namo-MacBook-Pro:~/Google Drive/Columbia/Course/ComputerNetworks/PA2/TcpAndRouterEmulation/GoBackN$ sh client_test.sh 
            node> send abcd
            [1491923275427] packet0 a sent
            [1491923275436] packet1 b sent
            [1491923275436] packet2 c sent
            [1491923275436] packet3 d sent
            [1491923275450] ACK0 received, window moves to 1
            [1491923275450] ACK0 discarded
            [1491923275954] packet1 timeout
            [1491923275954] packet1 b sent
            [1491923275954] packet2 c sent
            [1491923275954] packet3 d sent
            [1491923275954] ACK1 discarded
            [1491923275955] ACK2 discarded
            [1491923275955] ACK3 discarded
            [1491923276459] packet1 timeout
            [1491923276459] packet1 b sent
            [1491923276459] packet2 c sent
            [1491923276460] packet3 d sent
            [1491923276964] packet1 timeout
            [1491923276964] packet1 b sent
            [1491923276964] packet2 c sent
            [1491923276964] packet3 d sent
            [1491923276965] ACK3 discarded
            [1491923276965] ACK3 discarded
            [1491923276965] ACK3 received, window moves to 4
            [Summery] 6/8 packets discarded, loss rate = 75.0%
            node> 

        4.b.b terminal#2:
            namo@Namo-MacBook-Pro:~/Google Drive/Columbia/Course/ComputerNetworks/PA2/TcpAndRouterEmulation/GoBackN$ sh server_test.sh 
            node> [1491923275437] packet0 a received
            [1491923275448] ACK0 sent, expecting packet1
            [1491923275449] packet1 b discarded
            [1491923275449] packet2 c received
            [1491923275449] ACK0 sent, expecting packet1
            [1491923275450] packet3 d discarded
            [1491923275954] packet1 b received
            [1491923275954] ACK1 sent, expecting packet2
            [1491923275954] packet2 c received
            [1491923275955] ACK2 sent, expecting packet3
            [1491923275955] packet3 d received
            [1491923275955] ACK3 sent, expecting packet4
            [1491923276460] packet1 b discarded
            [1491923276460] packet2 c discarded
            [1491923276460] packet3 d discarded
            [1491923276964] packet1 b received
            [1491923276964] ACK3 sent, expecting packet4
            [1491923276965] packet2 c received
            [1491923276965] ACK3 sent, expecting packet4
            [1491923276965] packet3 d received
            [1491923276965] ACK3 sent, expecting packet4
            [Summery] 5/13 packets dropped, loss rate = 38.46153846153847%

    4.c Distance-Vector Routing Algorithm
        4.c.a run "1111_test.sh" in terminal#1
        4.c.b run "2222_test.sh" in terminal#2
        4.c.c run "3333_test.sh" in terminal#2
        4.c.d run "4444_test.sh" in terminal#2

    4.d Distance-Vector Routing Algorithm testing result
        4.d.a terminal#1:
            [1491925180988] Node 1111 Routing Table
            (1.4) -> Node 3333; Next hop -> Node 2222
            (0.9) -> Node 4444; Next hop -> Node 2222
            (0.1) -> Node 2222
            [1491925180997] Node 1111 Routing Table
            (0.5) -> Node 3333
            (0.9) -> Node 4444; Next hop -> Node 2222
            (0.1) -> Node 2222
            [1491925181011] Node 1111 Routing Table
            (0.3) -> Node 3333; Next hop -> Node 2222
            (0.8) -> Node 4444; Next hop -> Node 2222
            (0.1) -> Node 2222

        4.d.b terminal#2:
            [1491925180972] Node 2222 Routing Table
            (1.3) -> Node 3333; Next hop -> Node 4444
            (0.1) -> Node 1111
            (0.8) -> Node 4444
            [1491925180989] Node 2222 Routing Table
            (0.2) -> Node 3333
            (0.7) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 4444; Next hop -> Node 3333
            [1491925180996] Node 2222 Routing Table
            (0.2) -> Node 3333
            (0.5) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 4444; Next hop -> Node 3333
            [1491925181008] Node 2222 Routing Table
            (0.2) -> Node 3333
            (1.1) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 4444; Next hop -> Node 3333
            [1491925181011] Node 2222 Routing Table
            (0.2) -> Node 3333
            (0.1) -> Node 1111
            (0.7) -> Node 4444; Next hop -> Node 3333

        4.d.c terminal#3:
            [1491925180972] Node 3333 Routing Table
            (0.5) -> Node 1111
            (0.5) -> Node 4444
            (1.3) -> Node 2222; Next hop -> Node 4444
            [1491925180988] Node 3333 Routing Table
            (0.3) -> Node 1111; Next hop -> Node 2222
            (0.5) -> Node 4444
            (0.2) -> Node 2222
            [1491925180997] Node 3333 Routing Table
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.5) -> Node 4444
            (0.2) -> Node 2222
            [1491925181009] Node 3333 Routing Table
            (1.3) -> Node 1111; Next hop -> Node 2222
            (0.5) -> Node 4444
            (0.2) -> Node 2222
            [1491925181010] Node 3333 Routing Table
            (0.5) -> Node 1111
            (0.5) -> Node 4444
            (0.2) -> Node 2222
            [1491925181013] Node 3333 Routing Table
            (0.3) -> Node 1111; Next hop -> Node 2222
            (0.5) -> Node 4444
            (0.2) -> Node 2222

        4.d.d terminal#4:
            [1491925181002] Node 4444 Routing Table
            (2.1) -> Node 3333; Next hop -> Node 2222
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.8) -> Node 2222
            [1491925181005] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.8) -> Node 2222
            [1491925181015] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.8) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491925181017] Node 4444 Routing Table
            (0.5) -> Node 3333
            (1.4) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491925181019] Node 4444 Routing Table
            (0.5) -> Node 3333
            (1.0) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491925181022] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491925181023] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.8) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333

    4.e Combination
        4.e.a run "1111_test.sh" in terminal#1
        4.e.b run "2222_test.sh" in terminal#2
        4.e.c run "3333_test.sh" in terminal#2
        4.e.d run "4444_test.sh" in terminal#2

    4.f Combination testing result
        4.f.a terminal#1:
            [1491934610719] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934610720] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934611723] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934611723] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934612728] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934612728] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934613731] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934613731] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934614732] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934614732] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934615735] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934615735] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934616739] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934616739] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934617743] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934617743] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934618746] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934618746] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934619061] Node 1111 Routing Table
            (0.5) -> Node 3333; Next hop -> Node 2222
            (0.0) -> Node 4444; Next hop -> Node 2222
            (0.0) -> Node 2222
            [1491934619080] Node 1111 Routing Table
            (0.0) -> Node 3333
            (0.0) -> Node 4444; Next hop -> Node 2222
            (0.0) -> Node 2222
            [1491934619749] Link to 3333: 14 packets sent, 5 packets lost, loss rate 0.36
            [1491934619749] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934620752] Link to 3333: 22 packets sent, 8 packets lost, loss rate 0.36
            [1491934620753] Link to 2222: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934621757] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934621757] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934622762] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934622762] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934623765] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934623766] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934624769] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934624769] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934625773] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934625773] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934626774] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934626774] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934627779] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934627780] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934628781] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934628781] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934629786] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934629786] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934630789] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934630789] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934631793] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934631793] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934632794] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934632794] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934633799] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934633799] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934634804] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934634805] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934635809] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934635811] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934636814] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934636814] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934637819] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934637819] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934638824] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934638824] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934639826] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934639826] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934640830] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934640830] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934641835] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934641835] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934642840] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934642840] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934643844] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934643844] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934644069] Node 1111 Routing Table
            (0.0) -> Node 3333; Next hop -> Node 2222
            (0.81) -> Node 4444; Next hop -> Node 2222
            (0.0) -> Node 2222
            [1491934644071] Node 1111 Routing Table
            (0.0) -> Node 3333; Next hop -> Node 2222
            (0.76) -> Node 4444; Next hop -> Node 3333
            (0.0) -> Node 2222
            [1491934644074] Node 1111 Routing Table
            (0.0) -> Node 3333; Next hop -> Node 2222
            (0.38) -> Node 4444; Next hop -> Node 2222
            (0.0) -> Node 2222
            [1491934644844] Link to 3333: 24 packets sent, 9 packets lost, loss rate 0.38
            [1491934644844] Link to 2222: 9 packets sent, 0 packets lost, loss rate 0.0
            .
            .
            .

        4.f.b terminal#2:
            [1491934614706] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934614706] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934615708] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934615708] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934616712] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934616712] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934617714] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934617714] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934618718] Link to 3333: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934618718] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934619049] Node 2222 Routing Table
            (0.5) -> Node 3333; Next hop -> Node 4444
            (0.1) -> Node 1111
            (0.0) -> Node 4444
            [1491934619064] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.5) -> Node 1111; Next hop -> Node 3333
            (0.0) -> Node 4444
            [1491934619065] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.3) -> Node 1111; Next hop -> Node 3333
            (0.0) -> Node 4444
            [1491934619072] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.5) -> Node 1111; Next hop -> Node 3333
            (0.0) -> Node 4444
            [1491934619073] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.3) -> Node 1111; Next hop -> Node 3333
            (0.0) -> Node 4444
            [1491934619079] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.5) -> Node 1111; Next hop -> Node 3333
            (0.0) -> Node 4444
            [1491934619082] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.1) -> Node 1111
            (0.0) -> Node 4444
            [1491934619723] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934619723] Link to 4444: 10 packets sent, 8 packets lost, loss rate 0.8
            [1491934620728] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934620728] Link to 4444: 20 packets sent, 15 packets lost, loss rate 0.75
            [1491934621732] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934621732] Link to 4444: 30 packets sent, 24 packets lost, loss rate 0.8
            [1491934622734] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934622734] Link to 4444: 42 packets sent, 34 packets lost, loss rate 0.81
            [1491934623738] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934623739] Link to 4444: 52 packets sent, 41 packets lost, loss rate 0.79
            [1491934624744] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934624744] Link to 4444: 62 packets sent, 49 packets lost, loss rate 0.79
            [1491934625749] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934625749] Link to 4444: 72 packets sent, 57 packets lost, loss rate 0.79
            [1491934626752] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934626752] Link to 4444: 82 packets sent, 66 packets lost, loss rate 0.8
            [1491934627755] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934627755] Link to 4444: 92 packets sent, 75 packets lost, loss rate 0.82
            [1491934628758] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934628759] Link to 4444: 104 packets sent, 84 packets lost, loss rate 0.81
            [1491934629763] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934629763] Link to 4444: 114 packets sent, 93 packets lost, loss rate 0.82
            [1491934630764] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934630765] Link to 4444: 123 packets sent, 101 packets lost, loss rate 0.82
            [1491934631770] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934631770] Link to 4444: 131 packets sent, 108 packets lost, loss rate 0.82
            [1491934632773] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934632773] Link to 4444: 139 packets sent, 115 packets lost, loss rate 0.83
            [1491934633774] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934633774] Link to 4444: 147 packets sent, 121 packets lost, loss rate 0.82
            [1491934634780] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934634780] Link to 4444: 153 packets sent, 127 packets lost, loss rate 0.83
            [1491934635784] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934635784] Link to 4444: 159 packets sent, 132 packets lost, loss rate 0.83
            [1491934636789] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934636789] Link to 4444: 165 packets sent, 137 packets lost, loss rate 0.83
            [1491934637792] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934637792] Link to 4444: 170 packets sent, 140 packets lost, loss rate 0.82
            [1491934638797] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934638797] Link to 4444: 174 packets sent, 143 packets lost, loss rate 0.82
            [1491934639802] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934639802] Link to 4444: 178 packets sent, 147 packets lost, loss rate 0.83
            [1491934640806] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934640807] Link to 4444: 182 packets sent, 149 packets lost, loss rate 0.82
            [1491934641811] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934641811] Link to 4444: 186 packets sent, 150 packets lost, loss rate 0.81
            [1491934642814] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934642814] Link to 4444: 186 packets sent, 150 packets lost, loss rate 0.81
            [1491934643818] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934643818] Link to 4444: 186 packets sent, 150 packets lost, loss rate 0.81
            [1491934644073] Node 2222 Routing Table
            (0.0) -> Node 3333
            (0.0) -> Node 1111
            (0.38) -> Node 4444; Next hop -> Node 3333
            [1491934644821] Link to 3333: 9 packets sent, 0 packets lost, loss rate 0.0
            [1491934644821] Link to 4444: 186 packets sent, 150 packets lost, loss rate 0.81
            .
            .
            .

        4.f.c terminal#3:
            [1491934617034] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934618039] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934619043] Link to 4444: 0 packets sent, 0 packets lost, loss rate 0.0
            [1491934619049] Node 3333 Routing Table
            (0.5) -> Node 1111
            (0.0) -> Node 4444
            (0.8) -> Node 2222; Next hop -> Node 4444
            [1491934619062] Node 3333 Routing Table
            (0.3) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.2) -> Node 2222
            [1491934619066] Node 3333 Routing Table
            (0.5) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.2) -> Node 2222
            [1491934619071] Node 3333 Routing Table
            (0.3) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.0) -> Node 2222
            [1491934619074] Node 3333 Routing Table
            (0.5) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.0) -> Node 2222
            [1491934619085] Node 3333 Routing Table
            (0.3) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.0) -> Node 2222
            [1491934619088] Node 3333 Routing Table
            (0.1) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444
            (0.0) -> Node 2222
            [1491934620046] Link to 4444: 10 packets sent, 4 packets lost, loss rate 0.4
            [1491934621051] Link to 4444: 22 packets sent, 11 packets lost, loss rate 0.5
            [1491934622053] Link to 4444: 32 packets sent, 13 packets lost, loss rate 0.41
            [1491934623059] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934624056] Node 3333 Routing Table
            (0.0) -> Node 1111; Next hop -> Node 2222
            (0.0) -> Node 4444; Next hop -> Node 2222
            (0.0) -> Node 2222
            [1491934624061] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934625065] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934626069] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934627071] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934628074] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934629079] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934630083] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934631088] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934632091] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934633093] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934634097] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934635102] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934636107] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934637111] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934638117] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934639120] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934640122] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934641128] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934642130] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934643133] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934644068] Node 3333 Routing Table
            (0.0) -> Node 1111; Next hop -> Node 2222
            (0.38) -> Node 4444
            (0.0) -> Node 2222
            [1491934644136] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            [1491934645141] Link to 4444: 34 packets sent, 13 packets lost, loss rate 0.38
            .
            .
            .

        4.f.d terminal#4:
            [1491934619062] Node 4444 Routing Table
            (1.3) -> Node 3333; Next hop -> Node 2222
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.8) -> Node 2222
            [1491934619067] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.8) -> Node 2222
            [1491934619069] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.8) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491934619071] Node 4444 Routing Table
            (0.5) -> Node 3333
            (1.0) -> Node 1111; Next hop -> Node 3333
            (0.7) -> Node 2222; Next hop -> Node 3333
            [1491934619074] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.8) -> Node 1111; Next hop -> Node 3333
            (0.5) -> Node 2222; Next hop -> Node 3333
            [1491934619076] Node 4444 Routing Table
            (0.5) -> Node 3333
            (1.0) -> Node 1111; Next hop -> Node 3333
            (0.5) -> Node 2222; Next hop -> Node 3333
            [1491934619083] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.9) -> Node 1111; Next hop -> Node 2222
            (0.5) -> Node 2222; Next hop -> Node 3333
            [1491934619091] Node 4444 Routing Table
            (0.5) -> Node 3333
            (0.6) -> Node 1111; Next hop -> Node 3333
            (0.5) -> Node 2222; Next hop -> Node 3333
            [1491934624056] Node 4444 Routing Table
            (0.38) -> Node 3333
            (0.48) -> Node 1111; Next hop -> Node 3333
            (0.38) -> Node 2222; Next hop -> Node 3333
            [1491934624059] Node 4444 Routing Table
            (0.38) -> Node 3333
            (0.76) -> Node 1111; Next hop -> Node 3333
            (0.38) -> Node 2222; Next hop -> Node 3333
            [1491934624060] Node 4444 Routing Table
            (0.38) -> Node 3333
            (0.38) -> Node 1111; Next hop -> Node 3333
            (0.38) -> Node 2222; Next hop -> Node 3333
            [1491934644069] Node 4444 Routing Table
            (0.81) -> Node 3333; Next hop -> Node 2222
            (0.81) -> Node 1111; Next hop -> Node 2222
            (0.81) -> Node 2222
            [1491934644070] Node 4444 Routing Table
            (0.38) -> Node 3333
            (0.38) -> Node 1111; Next hop -> Node 3333
            (0.38) -> Node 2222; Next hop -> Node 3333
