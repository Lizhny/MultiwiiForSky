package communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import com.lizhy.LizhySky.R;

import util.LogUtil;

public class BT extends Communication {

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    // Well known SPP UUID (will *probably* map to
    // RFCOMM channel 1 (default) if not in use);
    // see comments in onResume().
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Handler handler;

    public BT(Context context) {
        super(context);
        Enable();
    }

    @Override
    public void Enable() {
        Toast.makeText(context, "Starting Bluetooth", Toast.LENGTH_SHORT).show();
        LogUtil.d(TAG, "+++ Enable BT +++");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, context.getString(R.string.Bluetoothisnotavailable), Toast.LENGTH_LONG).show();
            // finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Starting Bluetooth...", Toast.LENGTH_SHORT).show();
            mBluetoothAdapter.enable();
            return;
        }

        LogUtil.d(TAG, "+++ DONE IN ON CREATE, GOT LOCAL BT ADAPTER +++");

    }

    @Override
    public void Connect(String address, int speed) {
        // Blocking connect, for a simple client nothing else can
        // happen until a successful connection is made, so we
        // don't care if it blocks.
        Toast.makeText(context, context.getString(R.string.Connecting), Toast.LENGTH_LONG).show();

        if (mBluetoothAdapter.isEnabled()) {
            try {

                GetRemoteDevice(address);
                btSocket.connect();
                Connected = true;

                LogUtil.d(TAG, "BT connection established, data transfer link open.");
                Toast.makeText(context, context.getString(R.string.Connected), Toast.LENGTH_LONG).show();

                // app.Speak("Connected");

            } catch (IOException e) {
                try {
                    btSocket.close();
                    Connected = false;

                    Toast.makeText(context, context.getString(R.string.Unabletoconnect), Toast.LENGTH_LONG).show();
                    // app.Speak("Unable to connect");

                } catch (IOException e2) {
                    LogUtil.e(TAG, "ON RESUME: Unable to close socket during connection failure", e2);
                    Toast.makeText(context, "Connection failure", Toast.LENGTH_LONG).show();

                }
            }

            // Create a data stream so we can talk to server.
            LogUtil.d(TAG, "+ getOutputStream  getInputStream +");

            try {
                outStream = btSocket.getOutputStream();
                inStream = btSocket.getInputStream();

            } catch (IOException e) {
                LogUtil.e(TAG, "ON RESUME: Output stream creation failed.", e);
                Toast.makeText(context, "Stream creation failed", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean dataAvailable() {
        boolean a = false;

        try {
            if (Connected)
                a = inStream.available() > 0;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return a;
    }

    @Override
    public byte Read() {
        byte a = 0;
        try {
            a = (byte) inStream.read();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Read error", Toast.LENGTH_LONG).show();
        }
        return a;
    }

    @Override
    public void Write(byte[] arr) {
        try {
            if (Connected)
                outStream.write(arr);
        } catch (IOException e) {
            LogUtil.e(TAG, "SEND : Exception during write.", e);
            CloseSocket();

            Toast.makeText(context, "Write error", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void Close() {
        CloseSocket();

    }

    @Override
    public void Disable() {
        try {
            mBluetoothAdapter.disable();
        } catch (Exception e) {
            Toast.makeText(context, "Can't disable BT", Toast.LENGTH_LONG).show();
        }

    }

    @SuppressLint("NewApi")
    private void GetRemoteDevice(String address) {
        LogUtil.d(TAG, "+ ON RESUME +");
        LogUtil.d(TAG, "+ ABOUT TO ATTEMPT CLIENT CONNECT +");

        /**
         * When this returns, it will 'know' about the server,via it's MAC address.
         */
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        /**
         *  We need two things before we can successfully connect
         (authentication issues aside): a MAC address, which we
         already have, and an RFCOMM channel.
         Because RFCOMM channels (aka ports) are limited in
         number, Android doesn't allow you to use them directly;
         instead you request a RFCOMM mapping based on a service
         ID. In our case, we will use the well-known SPP Service
         ID. This ID is in UUID (GUID to you Microsofties)
         format. Given the UUID, Android will handle the
         mapping for you. Generally, this will return RFCOMM 1,
         but not always; it depends what other BlueTooth services
         are in use on your Android device.
         */
        try {

            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            LogUtil.e(TAG, "ON RESUME: Socket creation failed.", e);
            Toast.makeText(context, context.getString(R.string.Unabletoconnect), Toast.LENGTH_LONG).show();
        }

        // Discovery may be going on, e.g., if you're running a
        // 'scan for devices' search from your handset's Bluetooth
        // settings, so we call cancelDiscovery(). It doesn't hurt
        // to call it, but it might hurt not to... discovery is a
        // heavyweight process; you don't want it in progress when
        // a connection attempt is made.
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    /**
     * 关闭 Socket
     */
    public void CloseSocket() {
        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                LogUtil.e(TAG, "ON PAUSE: Couldn't flush output stream.", e);
                Toast.makeText(context, "Unable to close socket", Toast.LENGTH_LONG).show();
            }
        }

        try {
            if (btSocket != null)
                btSocket.close();
            Connected = false;

            Toast.makeText(context, context.getString(R.string.Disconnected), Toast.LENGTH_LONG).show();
            // app.Speak("Disconnected");

        } catch (Exception e2) {
            LogUtil.e(TAG, "ON PAUSE: Unable to close socket.", e2);
            Toast.makeText(context, "Unable to close socket", Toast.LENGTH_LONG).show();
        }

    }

}
