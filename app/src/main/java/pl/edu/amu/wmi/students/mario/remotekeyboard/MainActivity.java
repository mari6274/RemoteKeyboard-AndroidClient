package pl.edu.amu.wmi.students.mario.remotekeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import pl.edu.amu.wmi.students.mario.remotekeyboard.task.SendDatagramPacketAsyncTaskFactory;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private static final String IP_KEY = "ip";
    private static final String PORT_KEY = "port";
    private DatagramSocket datagramSocket;
    private String ip;
    private int port;
    private SendDatagramPacketAsyncTaskFactory sendDatagramPacketAsyncTaskFactory;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_server_config:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(R.string.server_config);
                final EditText ipEditText = new EditText(this);
                ipEditText.setHint(R.string.ip_address);
                ipEditText.setText(ip);
                final EditText portEditText = new EditText(this);
                portEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                portEditText.setHint(R.string.port_number);
                portEditText.setText(String.valueOf(port));
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(ipEditText);
                linearLayout.addView(portEditText);
                alertBuilder.setView(linearLayout);

                alertBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ip = ipEditText.getText().toString();
                        port = Integer.parseInt(portEditText.getText().toString());
                        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                        preferences.edit()
                                .putString(IP_KEY, ip)
                                .putInt(PORT_KEY, port)
                                .apply();
                    }
                });
                alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        ip = preferences.getString(IP_KEY, "localhost");
        port = preferences.getInt(PORT_KEY, 6274);

        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        sendDatagramPacketAsyncTaskFactory = new SendDatagramPacketAsyncTaskFactory(ip, port, datagramSocket, this);

        findViewById(R.id.touch_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int index = event.getActionIndex();
                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getAxisValue(MotionEvent.AXIS_X, index);
                        float y = event.getAxisValue(MotionEvent.AXIS_Y, index);
                        float lastX = x;
                        float lastY = y;
                        if (event.getHistorySize() > 0) {
                            lastX = event.getHistoricalAxisValue(MotionEvent.AXIS_X, index, event.getHistorySize() - 1);
                            lastY = event.getHistoricalAxisValue(MotionEvent.AXIS_Y, index, event.getHistorySize() - 1);
                        }
                        sendMouseMoveDatagramPacket(x - lastX, y - lastY);
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datagramSocket.close();
    }

    public void leftClick(View view) {
        sendKeyCodeDatagramPacket(0x25);
    }

    public void rightClick(View view) {
        sendKeyCodeDatagramPacket(0x27);
    }

    public void spaceClick(View view) {
        sendKeyCodeDatagramPacket(0x20);
    }

    public void mouseClick(View view) {
        sendMouseClickDatagramPacket();
    }

    private void sendKeyCodeDatagramPacket(final int keyCode) {
        sendDatagramPacketAsyncTaskFactory.createKeyCodeTask().execute(keyCode);
    }

    private void sendMouseMoveDatagramPacket(final float x, final float y) {
        sendDatagramPacketAsyncTaskFactory.createMouseMoveTask().execute(Math.round(x), Math.round(y));
    }

    private void sendMouseClickDatagramPacket() {
        sendDatagramPacketAsyncTaskFactory.createMouseClickTask().execute();
    }
}
