package stateMachine.state;

import lombok.Data;
import stateMachine.handler.IStateHandle;
import stateMachine.state.State;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
@Data
public class SopExec {
    private State nextState;
    private IStateHandle handler;
}
