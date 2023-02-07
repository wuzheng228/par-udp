package rdt.stop_wait_protocol.handler.receiver;

import rdt.stop_wait_protocol.PkgReceiver;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/25.
 */
public class ReceiverHandler implements IStateHandle<PkgReceiver, Event, Boolean> {
    @Override
    public Boolean handle(PkgReceiver pkgReceiver, Event event) {
        if (event == Event.RECEIVE_PKG) {
            Packet packet = new Packet(pkgReceiver.receivedFromChannel());
            System.out.println("pkg.seq expected rcv: " +  pkgReceiver.nextPacketExpected.get() + " rcv pkg seq: " + packet.seq);
            if (packet.isValid()) {
                if (pkgReceiver.nextPacketExpected.get() == packet.seq) {
                    pkgReceiver.saveToFile(packet);
                    pkgReceiver.increment();
                    // 触发 ACK 事件
                    pkgReceiver.onPositiveAck();
                    return true;
                } else {
                    System.out.println("....receive dulpicated packet");
                    // 说明服务端的ack 丢失了 客户端没有收到，所以服务端需要重新ack
                    // todo 这里需要重新保存文件
                    pkgReceiver.decrementFileReadOffset(packet.length);
                    pkgReceiver.saveToFile(packet);
                    // 触发 ACK 事件
                    pkgReceiver.onPositiveAck();
                    return true;
                }
            }
        }
        if (event == Event.RECEIVE_PKG_FIN) {
            Packet packet = new Packet(pkgReceiver.receivedFromChannel());
            System.out.println(packet);
            // 关闭输出流
            try {
                pkgReceiver.fileOutputStream.close();

                System.out.println("文件接收完成");
                //ack sender已经接收到
                Packet packetToSend = new Packet();
                packetToSend.ack =(byte) (pkgReceiver.nextPacketExpected.get());
                packetToSend.length = 0;
                pkgReceiver.sendToChannel(packetToSend);

                pkgReceiver.nextPacketExpected = new AtomicInteger(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        return false;
    }

    void deliverMessage(Packet packet) {
        byte[] payload = new byte[packet.length];
        for(int i=0; i<payload.length; i++)
            payload[i] = packet.payload[i];
        String recvd = new String(payload);
        System.out.println("Received "+packet.length+" bytes: "
                +new String(payload));
        //System.out.println("received payload len: "+recvd.length());
    }
}
