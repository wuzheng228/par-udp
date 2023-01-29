package stateMachine.handler.receiver;

import stateMachine.PkgReceiver;
import stateMachine.handler.IStateHandle;
import stateMachine.protocol.Packet;
import stateMachine.state.Event;

import java.nio.charset.StandardCharsets;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/25.
 */
public class InitHandler implements IStateHandle<PkgReceiver, Event,Boolean> {
    @Override
    public Boolean handle(PkgReceiver pkgReceiver, Event event) {

        Packet packet = new Packet(pkgReceiver.receivedFromChannel());

        byte[] paload = new byte[packet.length];
        for (int i = 0; i < packet.length; i++) {
            paload[i] = packet.payload[i];
        }

        if (packet.isValid()) {
            if (packet.syn == (byte) 1 && new String(paload, StandardCharsets.UTF_8).equals("SYN")) {
                // 发送 SYN_ACK
                System.out.println("发送SYN_ACK");
                Packet ack = new Packet();
                ack.ack = packet.seq;
                ack.payload = "SYN_ACK".getBytes(StandardCharsets.UTF_8);
                ack.syn = (byte) 1;

                pkgReceiver.sendToChannel(ack);
                return true;
            }
            if (packet.syn == (byte) 1 && new String(paload,StandardCharsets.UTF_8).equals("SYN_FIN")) {
                System.out.println("接收到SYN_FIN，连接建立完成");
                System.out.println("nextToSendSeq: " + pkgReceiver.nextPacketExpected);
                pkgReceiver.onPackageSYNFIN();
                return true;
            }
        }

        return false;
    }
}
