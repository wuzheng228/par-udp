package stateMachine.handler.receiver;

import stateMachine.PkgReceiver;
import stateMachine.handler.IStateHandle;
import stateMachine.protocol.Packet;
import stateMachine.state.Event;

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
