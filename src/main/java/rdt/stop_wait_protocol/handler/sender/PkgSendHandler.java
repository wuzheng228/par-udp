package rdt.stop_wait_protocol.handler.sender;

import rdt.stop_wait_protocol.PkgSender;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Event;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/23.
 */
public class PkgSendHandler implements IStateHandle<PkgSender, Event, Boolean> {
    @Override
    public Boolean handle(PkgSender sender, Event event) {
        sender.startTimer();
        return true;
    }
}
