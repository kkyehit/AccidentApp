package com.example.accident_alarm;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by 희준 on 2016-08-16.
 */
public class BT_service extends Service {

    private LinkedList<BluetoothDevice> mBluetoothDevices = new LinkedList<BluetoothDevice>();
    private ArrayAdapter<String> mDeviceArrayAdapter;


    public ProgressDialog mLoadingDialog;
    public AlertDialog mDeviceListDialog;
    public Blutooth_client mClient;

    static BluetoothDevice device;


    Intent noti;
    Intent broad_intent;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("send_broad")) {
                //if (MainActivity.btdevice != null) {
                device = MainActivity.device;
                test_toast();

                //}
            } else if (intent.getAction().equals("send_finish")) {
                //if (MainActivity.btdevice != null) {
                //notificationManager.cancel(7777);
                //android.os.Process.killProcess(android.os.Process.myPid());
                onDestroy();
            } else if (intent.getAction().equals("send_bt_1")){
                sendStringData("1");
            } else if (intent.getAction().equals("send_bt_2")){
                sendStringData("2");
            }
        }
    };

    IntentFilter intentFilter = new IntentFilter();

    private final IBinder mbinder= new LocalBinder();
    public class LocalBinder extends Binder {
        BT_service getService(){
            return BT_service.this;
        }
    }


    private void addDeviceToArrayAdapter(BluetoothDevice device) {
        if(mBluetoothDevices.contains(device)) {
            mBluetoothDevices.remove(device);
            mDeviceArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
        }
        mBluetoothDevices.add(device);
        mDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress() );
        mDeviceArrayAdapter.notifyDataSetChanged();
    }


    private void scanDevices() {
        Blutooth_client btSet = mClient;
        btSet.scanDevices(getApplicationContext(), new Blutooth_client.OnScanListener() {
            @Override
            public void onStart() {
                Log.d("Test", "Scan Start.");
            }

            /*기기 찾음*/
            @Override
            public void onFoundDevice(BluetoothDevice bluetoothDevice) {
                addDeviceToArrayAdapter(bluetoothDevice);
            }

            @Override
            public void onFinish() {
                Log.d("Test", "Scan finish.");
            }
        });
    }

    Notification notification;
    NotificationManager notificationManager;
    PendingIntent pendingIntent;
    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intentFilter.addAction("send_broad");
        intentFilter.addAction("send_finish");
        intentFilter.addAction("send_bt_1");
        intentFilter.addAction("send_bt_2");
        registerReceiver(broadcastReceiver,intentFilter);
   // Toast.makeText(getApplicationContext(), "Broadcast_Recieve_", Toast.LENGTH_SHORT).show();


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        intent = new Intent(BT_service.this , MainActivity.class);
        pendingIntent = PendingIntent.getActivities(BT_service.this , 0,
                new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);

        	/*클라이언트*/
        mClient = Blutooth_client.getInstance();

		/*클라이언트가 없는 경우*/
        if(mClient == null) {
            Toast.makeText(getApplicationContext(), "Cannot use the Bluetooth device.", Toast.LENGTH_SHORT).show();
        }
    }

    public void test_toast(){
        connect(device);
        sendStringData("0");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void connect(BluetoothDevice device) {
        Blutooth_client btSet =  mClient;
        sendStringData("");

        btSet.connect(getApplicationContext(), device, mBTHandler);

    }
    public void sendStringData(String data) {
        data += '\0';
        byte[] buffer = data.getBytes();
        if(mBTHandler.write(buffer)) {
            //Toast.makeText(getBaseContext(),"send",Toast.LENGTH_SHORT).show();
        }
    }

    private Blutooth_client.BluetoothStreamingHandler mBTHandler = new Blutooth_client.BluetoothStreamingHandler() {
        ByteBuffer mmByteBuffer = ByteBuffer.allocate(1024);

        @Override
        public void onError(Exception e) {
            // mLoadingDialog.cancel();
            //     addText("Messgae : Connection error - " +  e.toString() + "\n");
            // mMenu.getItem(0).setTitle(R.string.action_connect);
        }

        @Override
        public void onDisconnected() {
            //  mMenu.getItem(0).setTitle(R.string.action_connect);
            //mLoadingDialog.cancel();
            //      addText("Messgae : Disconnected.\n");
        }
        @Override
        public void onData(byte[] buffer, int length) {
            if(length == 0) return;
            if(mmByteBuffer.position() + length >= mmByteBuffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(mmByteBuffer.capacity() * 2);
                newBuffer.put(mmByteBuffer.array(), 0,  mmByteBuffer.position());
                mmByteBuffer = newBuffer;
            }
            mmByteBuffer.put(buffer, 0, length);
            if(buffer[length - 1] == '\0') {
                //Toast.makeText(getBaseContext(),mClient.getConnectedDevice().getName() + " : " +
                //        new String(mmByteBuffer.array(), 0, mmByteBuffer.position()),Toast.LENGTH_SHORT).show();
                if(new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).length()>1) {
                    Intent intent = new Intent(getBaseContext(),Accident.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Extra",1);
                    intent.putExtra("Lattitude",Search_Service.lattitude);
                    intent.putExtra("Longitude",Search_Service.longitude);
                    startActivity(intent);
                }
                mmByteBuffer.clear();
            }
        }

        @Override
        public void onConnected() {
            //addText("Messgae : Connected. " + mClient.getConnectedDevice().getName() + "\n");
            //  mLoadingDialog.cancel();
            //  mMenu.getItem(0).setTitle(R.string.action_disconnect);
        }
    };


    private String readCode() {
        try {
            InputStream is = getAssets().open("HC_06_Echo.txt");
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            is.close();
            String code = new String(buffer);
            buffer = null;
            return code;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}


