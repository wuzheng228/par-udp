package stateMachine.handler.sender;

import stateMachine.PkgSender;
import stateMachine.handler.IStateHandle;
import stateMachine.state.Event;

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
