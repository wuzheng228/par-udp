package stateMachine;

import stateMachine.handler.IStateHandle;
import stateMachine.handler.receiver.AckHandler;
import stateMachine.handler.receiver.InitHandler;
import stateMachine.handler.receiver.ReceiverHandler;
import stateMachine.protocol.Channel;
import stateMachine.protocol.NormalChannel;
import stateMachine.protocol.Packet;
import stateMachine.protocol.TransportLayer;
import stateMachine.state.Event;
import stateMachine.state.SopExec;
import stateMachine.state.SopProcess;
import stateMachine.state.State;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/25.
 */
public class PkgReceiver extends TransportLayer {

    public static final int LOCAL_PORT = 9888;
    public static final int REMOTE_PORT = 9887;

    public AtomicInteger nextPacketExpected = new AtomicInteger(0);

    public FileOutputStream fileOutputStream;

    public Long fileLength;
    public int fileReadOffset = 0;

    public PkgReceiver(Channel channel) {
        super(channel);
    }

    {
        List<SopProcess> sopProcesses = Arrays.asList(
                SopProcess.Builder.getInstance()
                        .from(State.INIT)
                        .to(State.INIT)
                        .event(Event.RECEIVE_PKG_SYN)
                        .handle(new InitHandler())
                        .build(),

                SopProcess.Builder.getInstance()
                        .from(State.INIT)
                        .to(State.WAIT_FOR_PACKET)
                        .event(Event.RECEIVE_PKG_SYN_FIN)
                        .build(),

                SopProcess.Builder.getInstance()
                        .from(State.WAIT_FOR_PACKET)
                        .to(State.POSITIVE_ACK)
                        .event(Event.RECEIVE_PKG)
                        .handle(new ReceiverHandler())
                        .build(),

                SopProcess.Builder.getInstance()
                        .from(State.WAIT_FOR_PACKET)
                        .to(State.INIT)
                        .event(Event.RECEIVE_PKG_FIN)
                        .handle(new ReceiverHandler())
                        .build(),

                SopProcess.Builder.getInstance()
                        .from(State.POSITIVE_ACK)
                        .to(State.WAIT_FOR_PACKET)
                        .event(Event.POCITIVE_ACK)
                        .handle(new AckHandler())
                        .build()
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
        while (true) {

            System.out.println("当前状态: " + currentState);

            Event event = waitForEvent();
            System.out.println("当前事件: " + event);

            SopExec sopExec = stateMachineMap.get(currentState, event);

            if (sopExec != null) {
                // 动作
                IStateHandle handler = sopExec.getHandler();
                Boolean pass = null;
                if (handler != null) {
                    pass = (Boolean) sopExec.getHandler().handle(this, event);
                }
                // 次态
                if (handler == null || (pass != null && pass)) {
                    System.out.println("次态: " + sopExec.getNextState());
                    setCurrentState(sopExec.getNextState());
                } else {
                    System.out.println("处理不通过，无法进行状态转移");
                }

            } else {
                System.out.println("unknow state and event= " + getCurrentState() + " " + getCurrentEvent());
            }
        }
    }

    public void increment() {
        if(nextPacketExpected.get() == 0)
            nextPacketExpected  = new AtomicInteger(1);
        else // seq number wrap around
            nextPacketExpected = new AtomicInteger(0);
    }

    public void saveToFile(Packet packet) {
        byte[] payload = new byte[packet.length];
        for(int i=0; i<payload.length; i++)
            payload[i] = packet.payload[i];
        try {
            fileOutputStream.getChannel().write(ByteBuffer.wrap(payload), fileReadOffset);
            incrementFileReadOffset(payload.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetFileReadOffset() {
        this.fileReadOffset = 0;
    }

    public void incrementFileReadOffset(int add) {
        this.fileReadOffset += add;
    }

    public void decrementFileReadOffset(int sub) {
        this.fileReadOffset -= sub;
    }

    public void setFileOutputStream(String file) {
        try {
            this.fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public static void main(String[] args) {
//        LossyChannel channel = new LossyChannel(LOCAL_PORT);
        NormalChannel channel = new NormalChannel(LOCAL_PORT);
        PkgReceiver pkgReceiver = new PkgReceiver(channel);
        channel.setTransportLayer(pkgReceiver);
        try {
            pkgReceiver.run();
        } finally {
            pkgReceiver.release();
        }
    }
}
