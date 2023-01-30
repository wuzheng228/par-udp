package stateMachine;

import stateMachine.handler.IStateHandle;
import stateMachine.handler.sender.InitHandler;
import stateMachine.handler.sender.InputHandler;
import stateMachine.handler.sender.PkgSendHandler;
import stateMachine.handler.sender.ReceiveHandler;
import stateMachine.protocol.*;
import stateMachine.state.Event;
import stateMachine.state.SopExec;
import stateMachine.state.SopProcess;
import stateMachine.state.State;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/22.
 */

public class PkgSender extends TransportLayer {

    public static final int RECEIVER_PORT = 9888;
    public static final int SENDER_PORT = 9887;

    public static final List<Packet> senderBuffer;

    static {
        senderBuffer = new ArrayList<>();
        senderBuffer.add(new Packet());
        senderBuffer.add(new Packet());
    }


    public AtomicInteger nextSeqToSend = new AtomicInteger(0);

    public static final Object inputLock = new Object();
    public static byte[] messageToSend;


    public InputTask inputThread = new InputTask(this);

    public PkgSender(Channel channel) {
        super(channel);
    }

    {
        // 初始化状态机
        List<SopProcess> sopProcesses = Arrays.asList(
                SopProcess.Builder.getInstance()
                        .from(State.INIT)
                        .to(State.INIT)
                        .event(Event.INIT)
                        .handle(new InitHandler()).build(), // 发送SYN 数据包

                SopProcess.Builder.getInstance()
                        .from(State.INIT)
                        .to(State.INIT)
                        .event(Event.TIME_OUT)
                        .handle(new InitHandler()).build(), // 处理超时

                SopProcess.Builder.getInstance()
                        .from(State.INIT)
                        .to(State.WAIT_FOR_INPUT)
                        .event(Event.RECEIVE_PKG_SYN)
                        .handle(new InitHandler())
                        .build(), //  处理接收数据包 SYN_ACK 停止定时器 发送 ACK


                SopProcess.Builder.getInstance()
                        .from(State.WAIT_FOR_INPUT)
                        .to(State.WAIT_FOR_ACK)
                        .event(Event.INPUT)
                        .handle(new InputHandler())
                        .build(), // 处理输入 发送message

                SopProcess.Builder.getInstance()
                        .from(State.WAIT_FOR_ACK)
                        .to(State.WAIT_FOR_ACK)
                        .event(Event.TIME_OUT)
                        .handle(new InputHandler())
                        .build(), // 开启定时器 处理超时事件

                SopProcess.Builder.getInstance()
                        .from(State.WAIT_FOR_ACK)
                        .to(State.WAIT_FOR_INPUT)
                        .event(Event.RECEIVE_PKG)
                        .handle(new ReceiveHandler()).build() // 处理输入

        );

        sopProcesses.forEach(item -> {
            SopExec sopExec = new SopExec();
            sopExec.setNextState(item.getTo());
            sopExec.setHandler(item.getStateHandle());
            stateMachineMap.put(item.getFrom(), item.getEvent(), sopExec);
        });

    }

    @Override
    public void run() {
        // 初始化
        SopExec sopExec = stateMachineMap.get(State.INIT, Event.INIT);
        sopExec.getHandler().handle(this, Event.INIT);
        State nextState = sopExec.getNextState();
        setCurrentState(nextState);

        while (true) {
            // 现态
            System.out.println("当前状态: " + getCurrentState().name());

            if (getCurrentState().name().equals("UNKNOW")) {
                release();
                break;
            }

            Event event = waitForEvent(); // main thread 进入wating set 等待唤醒

            System.out.println("发生事件: " + event);

            sopExec = stateMachineMap.get(getCurrentState(), event);

            if (sopExec != null) {
                // 动作
                IStateHandle handler = sopExec.getHandler();
                Boolean pass = null;

                if (handler != null) {
                    pass = (Boolean)handler.handle(this, event);
                }

                // 次态
                if (handler == null || (pass != null && pass)) {
                    System.out.println("次态: " + sopExec.getNextState());
                    setCurrentState(sopExec.getNextState());
                } else {
                    System.out.println("处理不通过，无法进行状态转移....");
                }
            } else {
                System.out.println("unknow state and event= " + getCurrentState() + " " + getCurrentEvent());
            }
        }
    }

    public void wakeUpInput() {
        synchronized (inputLock) {
            inputThread.canInput = true;
            inputLock.notifyAll();
        }
    }

    public void stopInput() {
        synchronized (inputLock) {
            inputThread.canInput = false;
            inputLock.notifyAll();
        }
    }

    public void increment() {
        if(nextSeqToSend.get() == 0)
            nextSeqToSend  = new AtomicInteger(1);
        else // seq number wrap around
            nextSeqToSend = new AtomicInteger(0);
    }

    public static class InputTask extends Thread {

        PkgSender sender;
        public boolean canInput = true;

        public InputTask(PkgSender transportLayer) {
            this.sender = transportLayer;
        }

        byte[] getMessageToSend() {
            System.out.println("Please enter a message to send: ");
            try {
                BufferedReader inFromUser =
                        new BufferedReader(new InputStreamReader(System.in));
                String sentence = inFromUser.readLine();
                if(null == sentence)
                    System.exit(1);
                System.out.println("Sending: "+sentence);

                return sentence.getBytes();
            } catch(Exception e) {
                System.out.println("IO error: "+e);
                return null;
            }
        }

        @Override
        public void run() {
            while (true) {
                synchronized (inputLock) {
                    waitForTrigger();
                    messageToSend = getMessageToSend();
                    sender.onInput();
                }
            }
        }

        void waitForTrigger() {
            while (!canInput) {
                try {
                    inputLock.wait();
                } catch (Exception e) {}
            }
            canInput = false;
        }
    }


    public static void main(String[] args) {
//        LossyChannel channel = new LossyChannel(SENDER_PORT, RECEIVER_PORT);
//        if (args.length < 1) {
//            throw new IllegalArgumentException("请输入远程服务host");
//        }

//        String remoteHost = args[0];

        NormalChannel channel = new NormalChannel(SENDER_PORT, "localhost",RECEIVER_PORT);
        PkgSender pkgSender = new PkgSender(channel);
        channel.setTransportLayer(pkgSender);
        pkgSender.run();
    }
}
