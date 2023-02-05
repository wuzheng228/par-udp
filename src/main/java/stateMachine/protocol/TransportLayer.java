package stateMachine.protocol;

import stateMachine.state.Event;
import stateMachine.state.State;
import stateMachine.state.StateMachineMap;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/19.
 */
public abstract class TransportLayer {

    private static final int MAX_SEQ = 1;
    private static final int TIME_OUT = 1000;

    // 包发送超时定时器, 用于超时重发
    public Timer timer;

    // 状态变量记录当前发生的事件
    public Event currentEvent = Event.INIT;
    // 状态变量记录当前的状态
    public State currentState = State.INIT;

    // 状态变量控制是否进行事件处理, 无事件 时 false， 发生事件时 true
    public boolean wakeUp = false;
    // 数据传输的通道
    public Channel channel;

    protected StateMachineMap stateMachineMap = new StateMachineMap();

    public ExecutorService executorService = Executors.newSingleThreadExecutor(); // 用于执行发送任务的线程

    public void release() {
        executorService.shutdownNow();
        if (timer != null) {
            timer.cancel();
        }

        channel.close();
    }

    public abstract void run();

    protected TransportLayer(Channel channel) {
        this.channel = channel;
    }

    /**
     * 通过网络层发送数据包
     * @param packet
     */
//    public void sendToChannelAndTriggerEvent(Packet packet) {
//        channel.send(packet.toBytes());
//        onPackageSend();
//    }

    /**
     * 通过网络层发送数据包
     * @param packet
     */
    public void sendToChannel(Packet packet) {
        channel.send(packet.toBytes());
    }


//    private synchronized void onPackageSend() {
//        wakeUp = true;
//        currentEvent = Event.SEND_PKG;
//        notifyAll();
//    }

    /**
     * 通过网络层接收数据包
     * @return
     */
    public byte[] receivedFromChannel() {
        return channel.receive();
    }

    /**
     * 开启定时器
     */
    public void startTimer() {
        timer = new Timer();
        timer.schedule(new SendTimerTask(), TIME_OUT);
    }

    /**
     * 停止定时器
     */
    public void stopTimer() {
        timer.cancel();
    }


    /**
     * 等待事件发生
     * @return 返回发送的时间类型
     */
    public synchronized Event waitForEvent() {
        while (!wakeUp) { // 调用
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        wakeUp = false;
        return currentEvent;
    }

    public synchronized void onPackageSYN() {
        wakeUp = true;
        currentEvent = Event.RECEIVE_PKG_SYN;
        notifyAll();
    }

    /**
     * 接收包事件
     */
    public synchronized void onPackageArrival() {
        wakeUp = true;
        currentEvent = Event.RECEIVE_PKG;
        notifyAll();
    }

    /**
     *
     */
    public synchronized void onWaitForInput() {
        wakeUp = true;
        currentEvent = Event.WAIT_FOR_INPUT;
        notifyAll();
    }

    /**
     * 定时器超时事件
     */
    public synchronized void onTimeout() {
        wakeUp = true;
        currentEvent = Event.TIME_OUT;
        notifyAll();
    }

    public synchronized void onInput() {
        wakeUp = true;
        currentEvent = Event.INPUT;
        notifyAll();
    }

    public synchronized void onPackageSYNFIN() {
        wakeUp = true;
        currentEvent = Event.RECEIVE_PKG_SYN_FIN;
        notifyAll();
    }

    public synchronized void onPositiveAck() {
        wakeUp = true;
        currentEvent = Event.POCITIVE_ACK;
        notifyAll();
    }

    /**
     * 获取当前的状态
     * @return
     */
    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        currentState = state;
    }

    /**
     * 获取当前的事件
     * @return
     */
    public synchronized Event getCurrentEvent() {
        return  currentEvent;
    }

    public synchronized void onFileReadEnd() {
        wakeUp = true;
        currentEvent = Event.FILE_READ_END;
        notifyAll();
    }

    public synchronized void onPackageFIN() {
        wakeUp = true;
        currentEvent = Event.RECEIVE_PKG_FIN;
        notifyAll();
    }

    public class SendTimerTask extends TimerTask {
        @Override
        public void run() {
            onTimeout();
        }
    }

}
