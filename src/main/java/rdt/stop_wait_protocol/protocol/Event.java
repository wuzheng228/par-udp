package rdt.stop_wait_protocol.protocol;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/19.
 */
public enum Event {
    INIT,
    TIME_OUT,
    RECEIVE_PKG,
    RECEIVE_PKG_SYN,
    RECEIVE_PKG_SYN_FIN,
    INPUT,
    POCITIVE_ACK,
    FILE_READ_END,
    RECEIVE_PKG_FIN,
    WAIT_FOR_INPUT
}