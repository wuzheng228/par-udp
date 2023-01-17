/*
 * Author: Wenbing Zhao
 * Last Modified: 10/4/2009
 * For EEC484 Project
 */

public class ParReceiver extends TransportLayer{
    public static final int RECEIVER_PORT = 9888;
    public static final int SENDER_PORT = 9887;

    public ParReceiver(LossyChannel lc) {
        super(lc);
    }

    public void run() {
        byte nextPacketExpected = 0;
        Packet packetReceived = new Packet();
        Packet packetToSend = new Packet();

        System.out.println("Ready to receive: ");

        while(true) {
            int event = waitForEvent();
            if(EVENT_PACKET_ARRIVAL == event) {
                packetReceived = receiveFromLossyChannel();

                // To be completed for task#2
                // PAR protocol implementation: receiver side

                if (packetReceived.isValid()) {
                    if (nextPacketExpected == packetReceived.seq) {
                        deliverMessage(packetReceived);
                        nextPacketExpected = increment(nextPacketExpected);
                    } else {
                        System.out.println("...Receive dulpicated packet");
                    }
                }

                packetToSend.ack =(byte) (1 - nextPacketExpected);
                packetToSend.length = 0;
                sendToLossyChannel(packetToSend);
            }
        }
    }

    // To be modified for task#5
    //
    // We simply extract the payload and display it as a string in stdout
    void deliverMessage(Packet packet) {
        byte[] payload = new byte[packet.length];
        for(int i=0; i<payload.length; i++)
            payload[i] = packet.payload[i];
        String recvd = new String(payload);
        System.out.println("Received "+packet.length+" bytes: "
                +new String(payload));
        //System.out.println("received payload len: "+recvd.length());
    }

    public static void main(String args[]) throws Exception {
        LossyChannel lc = new LossyChannel(RECEIVER_PORT, SENDER_PORT);
        ParReceiver receiver = new ParReceiver(lc);
        lc.setTransportLayer(receiver);
        receiver.run();
    }
}  