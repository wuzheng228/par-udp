package rdt.stop_wait_protocol.handler.sender;

import rdt.stop_wait_protocol.PkgSender;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * INIT + INIT | SYN_ACK | SYN_TIME_OUT
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
public class InitHandler implements IStateHandle<PkgSender, Event, Boolean> {
    @Override
    public Boolean handle(PkgSender sender, Event currentEvent) {
//        Event currentEvent = sender.getCurrentEvent();// 这里不应该再去读取一遍事件，可能已经改变了

        if (currentEvent == Event.INIT) {
            Packet packet = new Packet();
            packet.setSeq((byte) sender.nextSeqToSend.get());
            packet.setSyn((byte)1);

            try {
                sender.fileInputStream = new FileInputStream(PkgSender.transFile);
                FileChannel fileChannel = sender.fileInputStream.getChannel();
                long size = fileChannel.size();
                String payload = "SYN" + ";" +PkgSender.transFile.getName() + ";" + size;
                byte[] msgToSend = payload.getBytes(StandardCharsets.UTF_8);
                packet.payload = msgToSend;
                packet.length = msgToSend.length;
                sender.startTimer();
                sender.sendToChannel(packet);
            } catch (Exception e) {
                System.out.println("文件输入流异常" + e);
                return  false;
            }
            return true;
        }

        if (currentEvent == Event.RECEIVE_PKG_SYN) {
            Packet packet = new Packet(sender.receivedFromChannel());
            if (packet.isValid()
                    && packet.syn == (byte) 1
                    && packet.ack == sender.nextSeqToSend.get())
            {
                sender.stopTimer();

                Packet ack = new Packet();
                ack.seq = (byte) 1;
                ack.syn = 2;
                ack.payload = "SYN_FIN".getBytes(StandardCharsets.UTF_8);
                ack.length = "SYN_FIN".getBytes(StandardCharsets.UTF_8).length;

                sender.sendToChannel(ack);
                System.out.println("nextToSendSeq: " + sender.nextSeqToSend);
                // todo 这里应该开启另外一个线程 处理用户输入
                sender.inputThread.start();
                return true;
            }
        }

        if (currentEvent == Event.TIME_OUT) {
            Packet packet = new Packet();
            try {
                sender.fileInputStream = new FileInputStream(PkgSender.transFile);
                FileChannel fileChannel = sender.fileInputStream.getChannel();
                long size = fileChannel.size();
                String payload = "SYN" + ";" +PkgSender.transFile.getName() + ";" + size;
                byte[] msgToSend = payload.getBytes(StandardCharsets.UTF_8);
                packet.payload = msgToSend;
                packet.length = msgToSend.length;
                sender.sendToChannel(packet);
                sender.startTimer();
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return false;

    }
}
