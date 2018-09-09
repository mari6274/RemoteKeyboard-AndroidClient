package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import java.nio.ByteBuffer;

public class MouseMoveBufferInitializer implements BufferInitializer<Integer> {
    @Override
    public byte[] initialize(ByteBuffer byteBuffer, Integer... params) {
        return byteBuffer.put(PacketTypes.MOUSE_MOVE).putInt(params[0]).putInt(params[1]).array();
    }
}
