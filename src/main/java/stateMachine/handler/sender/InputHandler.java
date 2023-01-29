package stateMachine.handler.sender;

import stateMachine.PkgSender;
import stateMachine.handler.IStateHandle;
import stateMachine.protocol.Packet;
import stateMachine.state.Event;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/23.
 */
public class InputHandler implements IStateHandle<PkgSender, Event, Boolean> {
    @Override
    public Boolean handle(PkgSender sender, Event event) {
           synchronized (PkgSender.inputLock) {
               Packet packet = new Packet();
               packet.seq = (byte) sender.nextSeqToSend.get();
               packet.payload = PkgSender.messageToSend;
               packet.length = PkgSender.messageToSend.length;

               sender.sendToChannel(packet);
               sender.startTimer();
           }
           return true;
    }
}
