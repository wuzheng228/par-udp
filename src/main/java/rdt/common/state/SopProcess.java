package rdt.common.state;

import lombok.Data;
import rdt.stop_wait_protocol.handler.IStateHandle;
import rdt.stop_wait_protocol.protocol.Event;
import rdt.stop_wait_protocol.protocol.State;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/23.
 */
@Data
@SuppressWarnings("all")
public class SopProcess {
    private State from;
    private State to;
    private Event event;
    private IStateHandle stateHandle;

    public static class Builder {

        private SopProcess sopProcess;

        private Builder() {

        }

        public static Builder getInstance() {
            Builder builder = new Builder();
            builder.sopProcess = new SopProcess();
            return  builder;
        }

        public Builder from(State state) {
            sopProcess.from = state;
            return this;
        }

        public Builder to(State state) {
            sopProcess.to = state;
            return this;
        }

        public Builder event(Event event) {
            sopProcess.event = event;
            return this;
        }

        public Builder handle(IStateHandle stateHandle) {
            sopProcess.stateHandle = stateHandle;
            return this;
        }

        public SopProcess build() {
            return sopProcess;
        }
    }
}
