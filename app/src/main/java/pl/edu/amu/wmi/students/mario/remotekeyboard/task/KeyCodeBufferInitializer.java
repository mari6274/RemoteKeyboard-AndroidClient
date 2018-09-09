package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import java.nio.ByteBuffer;

public class KeyCodeBufferInitializer implements BufferInitializer<Integer> {
    @Override
    public byte[] initialize(ByteBuffer byteBuffer, Integer... params) {
        return byteBuffer.put(PacketTypes.KEY_CODE).putInt(params[0]).array();
    }
}
