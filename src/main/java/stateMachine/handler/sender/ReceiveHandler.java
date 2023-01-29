package stateMachine.handler.sender;

import stateMachine.PkgSender;
import stateMachine.handler.IStateHandle;
import stateMachine.protocol.Packet;
import stateMachine.state.Event;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
public class ReceiveHandler implements IStateHandle<PkgSender, Event,Boolean> {
    @Override
    public Boolean handle(PkgSender sender, Event event) {
//        sender.executorService.execute(()->{

            Packet packet = new Packet(sender.receivedFromChannel());

            if (packet.isValid()) {
                if (packet.ack == (byte) sender.nextSeqToSend.get()) {
                    sender.stopTimer();
                    sender.increment();
                    sender.wakeUpInput();
                } else {
                    System.out.println("..receive duplicated ack");
                }
            }
            return true;
//        });
    }
}
