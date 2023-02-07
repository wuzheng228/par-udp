package rdt.stop_wait_protocol.handler.sender;

import rdt.stop_wait_protocol.PkgSender;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Packet;
import rdt.stop_wait_protocol.protocol.Event;

import java.util.ArrayList;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/23.
 */
public class InputHandler implements IStateHandle<PkgSender, Event, Boolean> {
    @Override
    public Boolean handle(PkgSender sender, Event event) {
       if (event == Event.INPUT) {
           synchronized (PkgSender.inputLock) {
               Packet packet = new Packet();
               packet.seq = (byte) sender.nextSeqToSend.get();
               packet.payload = PkgSender.messageToSend;
               packet.length = PkgSender.messageToSend.length;

               PkgSender.senderBuffer.set(sender.nextSeqToSend.get(), packet);

               System.out.println("pkg.seq to send: " +  packet.seq);
               sender.sendToChannel(packet);
               sender.startTimer();
               sender.stopInput();
           }
           return true;
       }
       if (event == Event.TIME_OUT) {
           System.out.println("Time out not receive ack try to resend pkg, pkg.seq is: " + sender.nextSeqToSend.get());
           sender.sendToChannel(PkgSender.senderBuffer.get(sender.nextSeqToSend.get()));
           sender.startTimer();
           return true;
       }
       return false;
    }

    public static void main(String[] args) {
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(1,1L);
        System.out.println(longs);
    }
}
