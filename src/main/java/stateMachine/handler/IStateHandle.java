package stateMachine.handler;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */
public interface IStateHandle<T,E, R> {
    R handle(T t, E event);
}
