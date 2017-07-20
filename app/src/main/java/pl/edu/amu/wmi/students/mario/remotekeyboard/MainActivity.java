package pl.edu.amu.wmi.students.mario.remotekeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final String IP_KEY = "ip";
    private static final String PORT_KEY = "port";
    private DatagramSocket datagramSocket;
    private String ip;
    private int port;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        datagramSocket.close();
    }

    public void leftClick(View view) {
        sendDatagramPacket(0x25);
    }

    public void rightClick(View view) {
        sendDatagramPacket(0x27);
    }

    public void spaceClick(View view) {
        sendDatagramPacket(0x20);
    }

    private void sendDatagramPacket(final int keyCode) {
        new AsyncTask<Integer, Void, Void>() {

            private boolean error;

            @Override
            protected Void doInBackground(Integer... params) {
                byte[] buff = ByteBuffer.allocate(4).putInt(keyCode).array();
                try {
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(ip, port));
                    datagramSocket.send(packet);
                } catch (IOException e) {
                    error = true;
                    Log.e("SocketError", "Error during sending packet", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (error) {
                    Toast.makeText(MainActivity.this, R.string.key_sending_error, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(keyCode);


    }
}
