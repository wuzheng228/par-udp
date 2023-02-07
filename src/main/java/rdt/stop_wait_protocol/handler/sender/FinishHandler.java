package rdt.stop_wait_protocol.handler.sender;

import rdt.stop_wait_protocol.PkgSender;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

public class FinishHandler implements IStateHandle<PkgSender, Event, Boolean> {

    @Override
    public Boolean handle(PkgSender sender, Event event) {
        if (Event.FILE_READ_END == event) {
            // 发送带有fin标识的包给receiver
            Packet packet = new Packet();
            packet.seq = (byte) sender.nextSeqToSend.get();
            packet.fin = (byte) 1;
            packet.length = 0;

            sender.startTimer();
            sender.sendToChannel(packet);
        }

        if (Event.TIME_OUT == event) {
            // 重发fin包
            Packet packet = new Packet();
            packet.seq = (byte) sender.nextSeqToSend.get();
            packet.fin = (byte) 1;
            packet.length = 0;

            sender.startTimer();
            sender.sendToChannel(packet);

        }

        if (Event.RECEIVE_PKG == event) {
            byte[] bytes = sender.receivedFromChannel();
            Packet packetRcv = new Packet(bytes);
            System.out.println(packetRcv);
            System.out.println(sender.nextSeqToSend.get());
            if (packetRcv.isValid() && packetRcv.ack == sender.nextSeqToSend.get()) {
                sender.stopTimer();
                sender.closeFileInputSteam();
                // todo 通知 inputThread close
                sender.inputThread.interrupt();
                System.out.println("文件发送成功");
                return true;
            }
            return false;
        }
        return true;
    }
}
