package stateMachine.handler.receiver;

import stateMachine.PkgReceiver;
import stateMachine.handler.IStateHandle;
import stateMachine.protocol.Packet;
import stateMachine.state.Event;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/25.
 */
public class ReceiverHandler implements IStateHandle<PkgReceiver, Event, Boolean> {
    @Override
    public Boolean handle(PkgReceiver pkgReceiver, Event event) {
        Packet packet = new Packet(pkgReceiver.receivedFromChannel());
        if (packet.isValid()) {
            if (pkgReceiver.nextPacketExpected.get() == packet.seq) {
                deliverMessage(packet);
                pkgReceiver.increment();
                // 触发 ACK 事件
                pkgReceiver.onPositiveAck();
            } else {
                System.out.println("....receive dulpicated packet");
            }
        }
        return true;
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
