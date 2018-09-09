package pl.edu.amu.wmi.students.mario.remotekeyboard.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import pl.edu.amu.wmi.students.mario.remotekeyboard.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class SendDatagramPacketAsyncTask<T> extends AsyncTask<T, Void, SendDatagramPacketAsyncTask.Result> {

    private static final int BUFFER_SIZE = 9;
    private final String ip;
    private final int port;
    private final DatagramSocket datagramSocket;
    private Context context;
    private BufferInitializer<T> bufferInitializer;

    SendDatagramPacketAsyncTask(String ip, int port, DatagramSocket datagramSocket, Context context,
                                BufferInitializer<T> bufferInitializer) {
        this.ip = ip;
        this.port = port;
        this.datagramSocket = datagramSocket;
        this.context = context;
        this.bufferInitializer = bufferInitializer;
    }

    @Override
    protected SendDatagramPacketAsyncTask.Result doInBackground(T... params) {
        byte[] buff = bufferInitializer.initialize(ByteBuffer.allocate(BUFFER_SIZE), params);
        try {
            DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(ip, port));
            datagramSocket.send(packet);
        } catch (IOException e) {
            Log.e("SocketError", "Result during sending packet", e);
            return Result.SOCKET_ERROR;
        }
        return Result.DONE;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (result == Result.SOCKET_ERROR) {
            Toast.makeText(context, R.string.key_sending_error, Toast.LENGTH_SHORT).show();
        }
    }

    enum Result {
        DONE, SOCKET_ERROR
    }
}
