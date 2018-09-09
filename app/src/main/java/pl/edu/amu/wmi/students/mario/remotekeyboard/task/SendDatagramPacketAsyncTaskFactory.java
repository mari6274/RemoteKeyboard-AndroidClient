package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import android.content.Context;

import java.net.DatagramSocket;

public class SendDatagramPacketAsyncTaskFactory {

    private final String ip;
    private final int port;
    private final DatagramSocket datagramSocket;
    private final Context context;
    private final BufferInitializer<Integer> keyCodeBufferInitializer = new KeyCodeBufferInitializer();
    private final BufferInitializer<Integer> mouseMoveBufferInitializer = new MouseMoveBufferInitializer();
    private final BufferInitializer<Void> mouseClickBufferInitializer = new MouseClickBufferInitializer();

    public SendDatagramPacketAsyncTaskFactory(String ip, int port, DatagramSocket datagramSocket, Context context) {
        this.ip = ip;
        this.port = port;
        this.datagramSocket = datagramSocket;
        this.context = context;
    }

    public SendDatagramPacketAsyncTask<Integer> createKeyCodeTask() {
        return new SendDatagramPacketAsyncTask<>(ip, port, datagramSocket, context, keyCodeBufferInitializer);
    }

    public SendDatagramPacketAsyncTask<Integer> createMouseMoveTask() {
        return new SendDatagramPacketAsyncTask<>(ip, port, datagramSocket, context, mouseMoveBufferInitializer);
    }

    public SendDatagramPacketAsyncTask<Void> createMouseClickTask() {
        return new SendDatagramPacketAsyncTask<>(ip, port, datagramSocket, context, mouseClickBufferInitializer);
    }
}
