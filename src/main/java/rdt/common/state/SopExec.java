package rdt.common.state;

import lombok.Data;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.State;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
@Data
public class SopExec {
    private State nextState;
    private IStateHandle handler;
}
