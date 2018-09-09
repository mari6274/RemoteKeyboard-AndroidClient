package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import java.nio.ByteBuffer;

public interface BufferInitializer<T> {
    byte[] initialize(ByteBuffer byteBuffer, T... params);
}
