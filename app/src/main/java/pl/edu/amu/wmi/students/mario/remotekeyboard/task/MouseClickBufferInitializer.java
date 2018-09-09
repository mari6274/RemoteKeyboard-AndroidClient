package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import java.nio.ByteBuffer;

public class MouseClickBufferInitializer implements BufferInitializer<Void> {
    @Override
    public byte[] initialize(ByteBuffer byteBuffer, Void... params) {
        return byteBuffer.put(PacketTypes.MOUSE_CLICK).array();
    }
}
