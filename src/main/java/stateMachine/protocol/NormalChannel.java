package stateMachine.protocol;

import java.net.DatagramPacket;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
public class NormalChannel extends Channel {

    public NormalChannel(int localPort, int remotePort) {
        super(localPort, remotePort);
    }

    public NormalChannel(int localPort, String remoteHost, int remotePort) {
        super(localPort, remoteHost, remotePort);
    }

    public NormalChannel(int localPort, int remotePort, String ip) {
        super(localPort, remotePort, ip);
    }

    public NormalChannel(int localPort) {
        super(localPort);
    }

    @Override
    void send(byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress, remotePort);
            socket.send(packet);
        } catch (Exception e) {
            System.out.println("Error sending packet: "+e);
        }

    }

    @Override
    byte[] receive() {
        return receiveBuffer;
    }
}
