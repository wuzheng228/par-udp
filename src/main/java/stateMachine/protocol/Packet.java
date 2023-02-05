package stateMachine.protocol;

import base.ByteArrayUtils;
import lombok.Data;

/**
 * @Author by wuzheng.warms
 * @date 2023/1/21.
 */
@Data
public class Packet {
    // 魔数 用于协议校验
    public static final byte[] PREAMBLE = {
            'E', 'E', '4', '8', '4', 'F', 'A'
    };
    public static final int MAX_PACKET_PALOAD = 1024;
    public static final int MAX_PACKET_SIZE = MAX_PACKET_PALOAD + PREAMBLE.length + 8;

    public byte seq;
    public byte ack;
    public byte syn;
    public byte fin;
    public int length;
    public byte[] payload = new byte[MAX_PACKET_PALOAD];

    private boolean valid = true;

    public Packet() {
        seq = -1;
        ack = -1;
        syn = -1;
        fin = -1;
        length = 0;
    }

    /**
     * 字节数据转化为packet
     * @param data
     */
    public Packet(byte[] data) {
        boolean valid = verifyPacket(data);
        if (valid) {
            int index = PREAMBLE.length;
            seq = data[index++];
            ack = data[index++];
            syn = data[index++];
            fin = data[index++];

            byte[] intArray = {data[index], data[index + 1], data[index + 2], data[index + 3]};
            index += 4;

            int payloadLen = ByteArrayUtils.readInt(intArray);

            length = payloadLen;
            for (int i = 0; i < payloadLen; i++) {
                payload[i] = data[index + i];
            }
        }
    }

    public byte[] toBytes() {
        int totalLen = length + 4 + 4 + PREAMBLE.length;
        byte[] data = new byte[totalLen];
        // 填充首部
        int i = 0;
        for (; i < PREAMBLE.length; i++) {
            data[i] = PREAMBLE[i];
        }
        // 填充首部
        data[i++] = seq;
        data[i++] = ack;
        data[i++] = syn;
        data[i++] = fin;

        byte[] intArray = new byte[4];
        ByteArrayUtils.writeInt(intArray, length);
        for (int k = 0; k < 4; k++) {
            data[i++] = intArray[k];
        }

        // 填充数据部分
        int offset = i;
        for (;i < totalLen; i++) {
            data[i] = payload[i - offset];
        }

        return data;
    }


    /**
     * 校验packet 前缀 是否符合 preamble
     * @param data
     * @return
     */
    public boolean verifyPacket(byte[] data) {
        boolean verified = true;
        for (int i = 0; i < PREAMBLE.length; i++) {
            if (data[i] != PREAMBLE[i]) {
                verified = false;
                break;
            }
        }
        return verified;
    }

    /**
     * packet 有效性 有效：true， 无效：false
     * @return
     */
    public boolean isValid() {
        return valid;
    }


    public String toString() {
        return "pkg seq: " + seq + " ack: " + ack + " syn: " + syn + "fin " + fin;
    }

    public static void main(String[] args) {
        Packet packet = new Packet();
        byte[] bytes = packet.toBytes();

        Packet packet1 = new Packet(bytes);

        System.out.println(packet1);
    }
}
