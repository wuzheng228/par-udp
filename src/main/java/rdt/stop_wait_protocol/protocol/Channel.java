package rdt.stop_wait_protocol.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/19.
 */
public abstract class Channel {

    public static final Object addressLock = new Object();
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

    protected Channel(int localPort,String remoteHost ,int remotePort) {
        this.localPort = localPort;
        this.remotePort = remotePort;
        try {
            remoteAddress = Inet4Address.getByName(remoteHost);;
            socket = new DatagramSocket(localPort);
        } catch (Exception e) {
            throw new RuntimeException("Can not create UDP socket");
        }
        readThread = Executors.newSingleThreadExecutor();
        readThread.execute(new ReadTask());
    }

    protected Channel(int localPort,int remotePort, String ip) {
        this.localPort = localPort;
        this.remotePort = remotePort;
        try {

            byte[] ips = new byte[4];
            int i = 0;
            for (String each : ip.split("\\.")) {
                ips[i++] = (byte) Integer.parseInt(each);
            }
            remoteAddress = Inet4Address.getByAddress(ips);;
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
            System.out.println("channel read thread shutdown");
            socket.close();
            readThread.shutdownNow();
            System.out.println(readThread.isShutdown());
        }
    }

    public void setRemoteAddressAndPort(InetAddress address, int port) {
        synchronized (addressLock) {
            remoteAddress = address;
            remotePort = port;
            System.out.println("remoteAddress: " + remoteAddress);
            System.out.println("remotePort: " + remotePort);
        }
    }

    public class ReadTask implements Runnable {

        @Override
        public void run() {
            while (true) {
                System.out.println(Thread.currentThread().getName());
                DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    // 经过测试UDP发送数据到远端 服务器 客户端的 ip：port会发生改变
                    socket.receive(datagramPacket);
                    System.out.println("Channel read thread pkg's remoteAddress is " + datagramPacket.getAddress() + " port is " + datagramPacket.getPort());
                    setRemoteAddressAndPort(datagramPacket.getAddress(), datagramPacket.getPort());
                } catch (Exception e) {
                    System.out.println("Cannot receive from socket: " + e);
                    break;
                }

                if (transportLayer != null) {
                    if (receiveBuffer[Packet.PREAMBLE.length + 2] == (byte) 1 || receiveBuffer[Packet.PREAMBLE.length + 2] == (byte) 2) {
                        transportLayer.onPackageSYN();
                        continue;
                    }
                    if (receiveBuffer[Packet.PREAMBLE.length + 3] == (byte) 1) {
                        transportLayer.onPackageFIN();
                        continue;
                    }
                    transportLayer.onPackageArrival();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println((byte)172);
    }
}
