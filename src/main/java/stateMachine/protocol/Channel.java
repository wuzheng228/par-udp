package stateMachine.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/19.
 */
public abstract class Channel {

    protected InetAddress remoteAddress; // 远程服务器地址
    public DatagramSocket socket = null; // 绑定在本地端口的socket对象
    protected int localPort; // 本地接收数据的端口
    protected int remotePort; // 发送数据的的端口

    protected byte[] receiveBuffer = new byte[Packet.MAX_PACKET_SIZE];
    protected TransportLayer transportLayer;

    ExecutorService readThread;

    protected Channel(int localPort, int remotePort) {
        this.localPort = localPort;
        this.remotePort = remotePort;
        try {
            remoteAddress = InetAddress.getByName("localhost");;
            socket = new DatagramSocket(localPort);
        } catch (Exception e) {
            throw new RuntimeException("Can not create UDP socket");
        }
        readThread = Executors.newSingleThreadExecutor();
        readThread.execute(new ReadTask());
    }

    protected Channel(int localPort) {
        this.localPort = localPort;
        try {
            socket = new DatagramSocket(localPort);
        } catch (Exception e) {
            throw new RuntimeException("Can not create UDP socket");
        }
        readThread = Executors.newSingleThreadExecutor();
        readThread.execute(new ReadTask());
    }

    abstract void send(byte[] data);
    abstract byte[] receive();

    public void setTransportLayer(TransportLayer transportLayer) {
        this.transportLayer = transportLayer;
    }

    public void close() {
        if (socket != null) {
            readThread.shutdownNow();
            socket.close();
        }
    }

    public void setRemoteAddressAndPort(InetAddress address, int port) {
        remoteAddress = address;
        remotePort = port;
        System.out.println("remoteAddress: " + remoteAddress);
        System.out.println("remotePort: " + remotePort);
    }

    public class ReadTask implements Runnable {

        private int cnt =0;

        @Override
        public void run() {
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(datagramPacket);
                } catch (Exception e) {
                    System.out.println("Cannot receive from socket: " + e);
                    throw new RuntimeException("Cannot receive from socket: " + e);
                }

                if (transportLayer != null) {
                    if (receiveBuffer[Packet.PREAMBLE.length + 2] == (byte) 1) {
                        cnt++;
                        transportLayer.onPackageSYN();
                        if (remoteAddress == null) {
                            setRemoteAddressAndPort(datagramPacket.getAddress(), datagramPacket.getPort());
                        }
                        continue;
                    }
                    transportLayer.onPackageArrival();
                }
            }
        }
    }
}