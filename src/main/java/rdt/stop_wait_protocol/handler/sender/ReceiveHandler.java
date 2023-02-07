package rdt.stop_wait_protocol.handler.sender;

import rdt.stop_wait_protocol.PkgSender;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

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
