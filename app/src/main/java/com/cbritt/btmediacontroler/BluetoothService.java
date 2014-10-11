package com.cbritt.btmediacontroler;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Carson on 9/4/2014.
 */
public class BluetoothService extends Service{

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String INTENT_TAG = "com.cbritt.BLUETOOTH_SERIAL_INPUT";
    public static final String TAG = "Bluetooth Service";
    public static final String BT_DEVICE = "bikebt";
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;

    private int mState;
    private BluetoothDevice bt_device;
    private ConnectThread mConnectThread;
    private BluetoothAdapter mBluetoothAdapter;
    private static ConnectedThread mConnectedThread;


    @Override
    public void onCreate(){
        Log.d(TAG, "Service Created");
        super.onCreate();
    }

    @Override
    public IBinder onBind(final Intent arg0){
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(STATE_NONE);
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId){
        Log.d(TAG, "Service Started");
        super.onStartCommand(intent, flags, startId);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {

            // get paired devices and get name device with matching name
            Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
            if (devices != null) {
                for (BluetoothDevice device : devices) {
                    Log.d(TAG, "Device: " + device.getName() +" "+ device.getAddress());
                    if (BT_DEVICE.equals(device.getName())) {
                        bt_device = device;
                        break;
                    }
                }
            }

            //connect to device
            if(bt_device != null) {
                this.connectToDevice(bt_device);
            }
        }
        return START_STICKY;
    }


    //===================================================
    // Bluetooth Helpers
    //===================================================

    //get connected state
    public synchronized int getState() {
        return mState;
    }

    //update connected state
    private synchronized void setState(int state) {
        Log.d(TAG, "Setting state from: " + mState + " to: " + state);
        mState = state;
    }

    //initiate device connection
    public synchronized void connectToDevice(BluetoothDevice device) {
        Log.d("Bluetooth", "Connecting to device");
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    //manage device once connected
    public synchronized void handleConnection(BluetoothSocket socket, BluetoothDevice device) {
        Log.d("Bluetooth", "Device Connected");
        // Cancel any existing threads
        //if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }

    //===================================================
    // Helper Classes
    //===================================================

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            handleConnection(mmSocket, mmDevice);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private InputStreamReader isr;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            int data;

            // Keep listening to the InputStream until an exception occurs
            Log.d(TAG, "Listing for BT input");
            while (true) {
                try {

                    // Read from the InputStream
                    isr = new InputStreamReader(mmInStream);
                    //bytes = mmInStream.read(buffer);
                    data = Character.getNumericValue(isr.read());

                    if(!"".equals(data)){
                        Log.d(TAG, "Input Found value - "+data);
                        Intent i = new Intent();
                        i.putExtra("serial_value", data);

                        i.setAction(INTENT_TAG);
                        sendBroadcast(i);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "EXCEPTION " + e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }
}