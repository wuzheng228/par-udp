package stateMachine.state;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/19.
 */
public enum State {
    INIT,
    WAIT_FOR_ACK,
    WAIT_FOR_INPUT,
    WAIT_FOR_PACKET,
    POSITIVE_ACK,
    UNKNOW
}
