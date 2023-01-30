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
            System.out.println("receive packet from server packet.ack = " + packet.ack + " sender next to send seq is: " + sender.nextSeqToSend.get());
            if (packet.isValid()) {
                if (packet.ack == (byte) sender.nextSeqToSend.get()) {
                    sender.stopTimer();
                    sender.increment();
                    sender.wakeUpInput();
                } else {
                    System.out.println("..receive duplicated ack");
                    // 说明这个包已经发过了 还需等待 ack = 已经发送包seq 的数据包，一直收不到就超时重传
                    return false;
                }
            }
            return true;
//        });
    }
}
