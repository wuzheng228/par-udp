package stateMachine.protocol;

import java.net.DatagramPacket;
import java.util.Random;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/27.
 */
public class LossyChannel extends Channel {

    public LossyChannel(int localPort, int remotePort) {
        super(localPort, remotePort);
    }

    public LossyChannel(int localPort) {
        super(localPort);
    }

    @Override
    void send(byte[] data) {
        Random rand = new Random();
        int randnum = rand.nextInt(10); // range 0-10
        if(randnum < 3) {
            System.out.println("net error occur pkg loss");
            return; // simulate a loss
        }

        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);
            socket.send(packet);
        } catch(Exception e) {
            System.out.println("Error sending packet: "+e);
        }
    }

    @Override
    byte[] receive() {
        return receiveBuffer;
    }
}
