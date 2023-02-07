package rdt.stop_wait_protocol.handler.receiver;

import rdt.stop_wait_protocol.PkgReceiver;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/27.
 */
public class AckHandler implements IStateHandle<PkgReceiver, Event,Boolean> {
    @Override
    public Boolean handle(PkgReceiver pkgReceiver, Event event) {
        Packet packetToSend = new Packet();
        packetToSend.ack =(byte) (1 - pkgReceiver.nextPacketExpected.get());
        packetToSend.length = 0;

        System.out.println("receicver the packet ack to the sender ack is: " + packetToSend.ack);
        pkgReceiver.sendToChannel(packetToSend);
        return true;
    }
}
