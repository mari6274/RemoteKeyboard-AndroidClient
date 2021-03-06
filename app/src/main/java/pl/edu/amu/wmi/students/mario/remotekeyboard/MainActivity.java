package pl.edu.amu.wmi.students.mario.remotekeyboard;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import pl.edu.amu.wmi.students.mario.remotekeyboard.task.SendDatagramPacketAsyncTaskFactory;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private static final String IP_KEY = "ip";
    private static final String PORT_KEY = "port";
    private static final int BAR_CODE_SCANNER_REQUEST_CODE = 0;
    private static final String ZXING_APP_URI = "https://play.google.com/store/apps/details?id=com.google.zxing.client.android";
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

    public void barCodeScannerClick(View view) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, BAR_CODE_SCANNER_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showInstallZXingDialog();
        }
    }

    private void showInstallZXingDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.missing_bar_code_scanner_app)
                .setMessage(R.string.install_zxing_bar_code_scanner)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ZXING_APP_URI)));
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == BAR_CODE_SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            String productCode = intent.getStringExtra("SCAN_RESULT");
            Log.d("SCAN_RESULT", productCode);
            sendBarCodeScannerKeyCombination(productCode);
        }
    }

    private void sendBarCodeScannerKeyCombination(String productCode) {
        sendKeyCodeDatagramPacket(KeyEvent.KEYCODE_F9);
        for (char ch : padRightWithSpacesTo15Length(productCode).toCharArray()) {
            sendKeyCodeDatagramPacket(ch);
        }
        sendKeyCodeDatagramPacket(KeyEvent.KEYCODE_F2);
    }

    private String padRightWithSpacesTo15Length(String productCode) {
        StringBuilder productCodeBuilder = new StringBuilder(productCode);
        while (productCodeBuilder.length() < 15) {
            productCodeBuilder.append(" ");
        }
        productCode = productCodeBuilder.toString();
        return productCode;
    }
}
