package com.example.phone_mouse;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    float initialX = 0;
    float initialY = 0;
    float[] relpos = new float[2];
    boolean connected = false;
    DatagramSocket socket;
    float sensitivity = 1.0f;
    String ip_address;
    int port;
    Toast sensitivityToast;

    private class SendPacket extends AsyncTask<DatagramPacket, Void, Void> {

        @Override
        protected Void doInBackground(DatagramPacket... packet) {
            try {
                socket.send(packet[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!connected) {
            return false;
        }
//        Log.d("num fing", String.valueOf(event.getPointerCount()));
        if (event.getPointerCount() == 2) {
//                Log.d("INMFO", event.toString());
            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                Log.d("ACTION", "2nd finger touched fire click");
                try {
                    DatagramPacket packet = new DatagramPacket("LCLICK".getBytes("UTF-8"), "LCLICK".getBytes("UTF-8").length, InetAddress.getByName(ip_address), port);
                    new SendPacket().execute(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                Log.d("ACTION", "2nd finger touch down click");
                try {
                    DatagramPacket packet = new DatagramPacket("LHOLD".getBytes("UTF-8"), "LHOLD".getBytes("UTF-8").length, InetAddress.getByName(ip_address), port);
                    new SendPacket().execute(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        } else {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    relpos[0] = event.getX();
                    relpos[1] = event.getY();
                    Log.d("ACTION", "Finger Touched at POS X: " + initialX + "Y: " + initialY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    relpos[0] = (relpos[0] - event.getX()) * sensitivity;
                    relpos[1] = (relpos[1] - event.getY()) * sensitivity;
                    Log.d("MOVE", "X: " + relpos[0] + " Y:" + relpos[1]);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(8);
                    byteBuffer.putFloat(relpos[0]);
                    byteBuffer.putFloat(relpos[1]);
                    try {
                        DatagramPacket packet = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length, InetAddress.getByName(ip_address), port);
                        new SendPacket().execute(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    relpos[0] = event.getX();
                    relpos[1] = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
//                if (event.getPointerCount() == 2) {
//                    Log.d("ACTION", "2nd finger touched fire click");
//                }
//                break;
                case MotionEvent.ACTION_CANCEL:

            }
        }
        return true;
    }

    public void onConnectClick(View view) {
        Log.d("CLICKED", "clicked");
        findViewById(R.id.connectButton).setVisibility(View.GONE);
        findViewById(R.id.ipBox).setVisibility(View.GONE);
        findViewById(R.id.portBox).setVisibility(View.GONE);

        Log.d("Destination", ((EditText) findViewById(R.id.ipBox)).getText().toString());


        try {
            socket = new DatagramSocket();
            ip_address = ((EditText) findViewById(R.id.ipBox)).getText().toString();
            port = Integer.parseInt(((EditText) findViewById(R.id.portBox)).getText().toString());
            connected = true;
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public void increaseSensitivity(View view) {
        if (sensitivity >= 10.0f) {
            toastMaker("Sensitivity at maximum", Toast.LENGTH_SHORT);
            return;
        }
        sensitivity += 0.1f;
        toastMaker("Sensitivity increased to " + String.format("%.2f", sensitivity), Toast.LENGTH_SHORT);
    }

    public void decreaseSensitivity(View view) {
        if (sensitivity <= 0.1f) {
            toastMaker("Sensitivity at minimum", Toast.LENGTH_SHORT);
            return;
        }
        sensitivity -= 0.1f;
        toastMaker("Sensitivity decreased to " + String.format("%.2f", sensitivity), Toast.LENGTH_SHORT);
    }

    private void toastMaker(String msg, int len) {
        if (sensitivityToast != null) {
            sensitivityToast.cancel();
        }

        sensitivityToast = Toast.makeText(getApplicationContext(), msg, len);
        sensitivityToast.show();
    }
}

