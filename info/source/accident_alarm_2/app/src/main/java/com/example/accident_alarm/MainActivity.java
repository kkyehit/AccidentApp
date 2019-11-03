package com.example.accident_alarm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.internal.zzc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Calendar calendar;

    Intent service_intent;
    Intent BT_service_intent;

    /*UI*/
    ImageButton accident, stop,bt_button;

    /*확인하는 dilog*/
    Check_Dialog dialog;

    /*사고 리스트*/
    Accident_List_Setting accident_list_setting;
    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    /*구글 맵과 센서 리스너*/
    GoogleMap _Map;
    SensorManager sensor_Manager;

    /*경도, 위도*/
    Double lattitude = new Double(999);
    Double longitude = new Double(999);

    /*버튼 이용*/
    boolean Compass_Enable;

    /*권한설정 android 23 이상*/
    String[] Permission_List = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET};

    /*sensor listener*/
    SensorEventListener senser_event_Listener = new SensorEventListener(){

        int iorientation = -1;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(iorientation<0){
                iorientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /*location listener*/
    public class My_Location_Listener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {// 위치가 변화면
            /*현제위치 받기*/
            lattitude = location.getLatitude();
            longitude = location.getLongitude();
            /*구글 맵을 현제위치로 이동*/
            LatLng current_point = new LatLng(lattitude, longitude);
            _Map.animateCamera(CameraUpdateFactory.newLatLngZoom(current_point, 20));
            _Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            /*서버와 연결*/
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    /**********
    *서버 관련*
    ***********/
    Handler post_handler;//

    BufferedWriter bufferedWriter;//서버에 보낼떄
    BufferedReader bufferedReader;//서버에서 받을 떄

    private String ip = "192.168.1.2";// ip
    private int Port = 9999; // prot

    Socket socket;//소캣, 통신

    int Thread_flag = 1;

    String send_msg, Receive_msg_main;

    /*list에 정보 업데이트*/
    private Runnable list_Update = new Runnable() {
        public void run() {
            listView.setAdapter(arrayAdapter);
        }
    };

    /*Socket 설정*/
    public void setSocket(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    /*Thread*/
    Thread server_thread = new Thread(){
        Double temp_lattitude = new Double(999),
                temp_longitude = new Double(999);
        @Override
        public void run() {
            super.run();
            while (Thread_flag == 1) {
                if(((!lattitude.equals(temp_lattitude))
                        &&(!longitude.equals(temp_longitude)))) {
                    temp_longitude = longitude;
                    temp_lattitude = lattitude;
                    calendar = new GregorianCalendar();
                    try {
                        try {//소캣 설정
                            setSocket(ip, Port);
                        } catch (IOException e) {
                        }
                        while (true) {
                            Receive_msg_main = bufferedReader.readLine();
                            if (Receive_msg_main.compareTo("ONLINE") == 0) {
                                PrintWriter out = new PrintWriter(bufferedWriter, true);
                                send_msg = "0/"+calendar.get(Calendar.DATE)+"/"+calendar.get(Calendar.HOUR)+":"
                                        +calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"/"
                                        +"Latting" + "/" + lattitude + "/" + longitude + "/";
                                out.println(send_msg);
                                while ((Receive_msg_main.compareTo("ONLINE") == 0) || (Receive_msg_main == null)) {
                                    Receive_msg_main = bufferedReader.readLine();
                                    Log.d("Chatting running", "chatting running");
                                }

                                accident_list_setting.add_array_list(Receive_msg_main);

                                post_handler.post(list_Update);

                                out.println("#33");
                                break;
                            }
                        }
                        Log.d("Socket close", "complet socket close");
                        socket.close();
                    } catch (Exception e) {
                        Log.d("error,", "[" + e + ")");
                    }
                }
            }
        }
    };

    /*BT*/
    private static LinkedList<BluetoothDevice> mBluetoothDevices = new LinkedList<BluetoothDevice>();
    private static ArrayAdapter<String> mDeviceArrayAdapter;

    private EditText mEditTextInput;
    private TextView mTextView;
    private Button mButtonSend;
    private ProgressDialog mLoadingDialog;
    private AlertDialog mDeviceListDialog;
    private Menu mMenu;

    private static Blutooth_client mClient;
    static BluetoothDevice device = null;
    Intent intent;
    Intent send_intent;

    /*File*/
    String string_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            FileInputStream Load_Use_file = openFileInput("number.txt");
            InputStreamReader Load_Use_Reader = new InputStreamReader(Load_Use_file);
            char[] buffer = new char[100];
            int charread;
            while((charread = Load_Use_Reader.read(buffer))>0){
                String temp = String.copyValueOf(buffer,0,charread);
                string_list += temp;
            }
            Load_Use_file.close();
            Load_Use_Reader.close();
        }catch (Exception e) {
            try {
                FileOutputStream Save_file = openFileOutput("number.txt", MODE_PRIVATE);
                OutputStreamWriter Save_file_writer = new OutputStreamWriter(Save_file);
                Save_file_writer.write("119/112");
                Save_file_writer.close();
                Save_file.close();
                Toast.makeText(getApplication(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } catch (java.io.IOException e2) {
                Toast.makeText(getApplication(), "오류가 발생하였습니다." +
                        " 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        service_intent = new Intent(MainActivity.this, Search_Service.class);
        BT_service_intent = new Intent(MainActivity.this,BT_service.class);

        /*맵 관련*/
        _Map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map_fragment)).getMap();
        sensor_Manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /*시간*/
        calendar = new GregorianCalendar();

        /*위치 정보 관련*/
        LocationManager L_M = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        My_Location_Listener L_L = new My_Location_Listener();

        /*권한 설정*/
//        if(Build.VERSION.SDK_INT>21) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getBaseContext(), "권한 설정 좀", Toast.LENGTH_LONG).show();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {

                } else {
                    ActivityCompat.requestPermissions(this, Permission_List, 1);
                }
            }
  //      }
        /*서버*/
        post_handler = new Handler();
        server_thread.start();

        /*위치 정보 받기 시작*/
//        L_M.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, L_L);
        L_M.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, L_L);

        /*리스트 정보 받음*/
        accident_list_setting = new Accident_List_Setting();

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, accident_list_setting.get_array_list());

        listView = (ListView) findViewById(R.id.near_list);
        listView.setAdapter(arrayAdapter);
        listView.setDividerHeight(2);
        listView.setBottom(5);

        /*리스트뷰 클릭하면 해당 위치로 이동*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String string = parent.getItemAtPosition(position).toString();
                String temp_string[] = string.split("/");
                double click_lattitue = Double.parseDouble(temp_string[0]);
                double click_longitude = Double.parseDouble(temp_string[1]);
                LatLng click_point = new LatLng(click_lattitue,click_longitude);
                _Map.animateCamera(CameraUpdateFactory.newLatLngZoom(click_point,20));
            }
        });

        /*버튼 설정*/
        accident = (ImageButton) findViewById(R.id.main_accident);
        stop = (ImageButton) findViewById(R.id.main_stop);
        bt_button = (ImageButton) findViewById(R.id.main_bt_button);

        /*버튼을 누르면 dialog 띄움*/
        dialog = new Check_Dialog();
        accident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.situation = 1;
                dialog.lattitude = lattitude;
                dialog.longitude = longitude;
                dialog.show(getFragmentManager(), "Chaek");
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.situation = 2;
                dialog.lattitude = lattitude;
                dialog.longitude = longitude;
                dialog.show(getFragmentManager(), "Chaek");
            }
        });
        bt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeviceListDialog.show();
            }
        });

        	/*클라이언트*/
        mClient = Blutooth_client.getInstance();

        /*클라이언트가 없는 경우*/
        if(mClient == null) {
            Toast.makeText(getApplicationContext(), "Cannot use the Bluetooth device.", Toast.LENGTH_SHORT).show();
            finish();
        }

        overflowMenuInActionBar();
        initProgressDialog();
        initDeviceListDialog();

        startService(service_intent);
        startService(BT_service_intent);
    }



    @Override
    public void onResume() {
        super.onResume();
        /*권한 설정*/
        /*권한 설정*/
//        if(Build.VERSION.SDK_INT>21) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "권한 설정 좀", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {

            } else {
                ActivityCompat.requestPermissions(this, Permission_List, 1);
            }
        }
        //      }

        enableBluetooth();
        /*맵의 버튼 사용*/
        _Map.setMyLocationEnabled(true);
        if(Compass_Enable){
            sensor_Manager.registerListener(senser_event_Listener,sensor_Manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_UI);
        }
        Thread_flag = 1;
    }
    @Override
    public void onPause() {
        super.onPause();
        mClient.cancelScan(getApplicationContext());
        super.onPause();
 //       if(Build.VERSION.SDK_INT>21) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getBaseContext(), "권한 설정 좀", Toast.LENGTH_LONG).show();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)
                        && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {

                } else {
                    ActivityCompat.requestPermissions(this, Permission_List, 1);
                }
            }
   //     }
        _Map.setMyLocationEnabled(false);
        if(Compass_Enable){
            sensor_Manager.unregisterListener(senser_event_Listener);
        }

    }

    /*어플이 종료되면*/
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        Thread_flag = 0;
    }



    /*BT*/
    /*메뉴버튼 클릭 시*/
    private void overflowMenuInActionBar(){
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // 무시한다. 3.x 이 예외가 발생한다.
            // 또, 타블릿 전용으로 만들어진 3.x 버전의 디바이스는 보통 하드웨어 버튼이 존재하지 않는다.
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
    }

    /*로딩 다이아로그 초기화*/
    private void initProgressDialog() {
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setCancelable(false);
    }

    /*리스트 다이아 그램 초기화*/
    private void initDeviceListDialog() {
        mDeviceArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_device);
        ListView listView = new ListView(getApplicationContext());
        listView.setAdapter(mDeviceArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item =  (String) parent.getItemAtPosition(position);
                for(int i = 0; i< mBluetoothDevices.size();i++) {
                    if(item.contains(mBluetoothDevices.get(i).getAddress())) {
						/*연결*/
                        //connect(device);
                        //Test_serial test_serial = new Test_serial(device);
                        //send_intent.putExtra("device",test_serial);
                        mDeviceListDialog.cancel();
                        device = mBluetoothDevices.get(i);
                        send_intent = new Intent("send_broad");
                        sendBroadcast(send_intent);
                        sendStringData("");
                    }
                }
            }
        });
		/*리스트 뷰를 갖는 alertdialog 생성*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select bluetooth device");
        builder.setView(listView);
        builder.setPositiveButton("Scan",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        scanDevices();
                    }
                });
        mDeviceListDialog = builder.create();
        mDeviceListDialog.setCanceledOnTouchOutside(false);
        //mDeviceListDialog.show();
    }

    /*arraylist에 디바이스 추가*/
    private void addDeviceToArrayAdapter(BluetoothDevice device) {
        if(mBluetoothDevices.contains(device)) {
            mBluetoothDevices.remove(device);
            mDeviceArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
        }
        mBluetoothDevices.add(device);
        mDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress() );
        mDeviceArrayAdapter.notifyDataSetChanged();

    }

    /*연결되있는 기기 정보 얻기*/
    private void enableBluetooth() {
        Blutooth_client btSet =  mClient;
        btSet.enableBluetooth(this, new Blutooth_client.OnBluetoothEnabledListener() {
            @Override
            public void onBluetoothEnabled(boolean success) {
                if(success) {
                    getPairedDevices();
                } else {
                    finish();
                }
            }
        });
    }

    private void addText(String text) {
        mTextView.append(text);
        final int scrollAmount = mTextView.getLayout().getLineTop(mTextView.getLineCount()) - mTextView.getHeight();
        if (scrollAmount > 0)
            mTextView.scrollTo(0, scrollAmount);
        else
            mTextView.scrollTo(0, 0);
    }

    /*페어링된적 있는 디바이스 추가*/
    private void getPairedDevices() {
        Set<BluetoothDevice> devices =  mClient.getPairedDevices();
        for(BluetoothDevice device: devices) {
            addDeviceToArrayAdapter(device);
        }
    }
    /*기기 검색*/
    private void scanDevices() {
        Blutooth_client btSet = mClient;
        btSet.scanDevices(getApplicationContext(), new Blutooth_client.OnScanListener() {
            String message ="";
            @Override
            public void onStart() {
                Log.d("Test", "Scan Start.");
                mLoadingDialog.show();
                message = "Scanning....";
                mLoadingDialog.setMessage("Scanning....");
                mLoadingDialog.setCancelable(true);
                mLoadingDialog.setCanceledOnTouchOutside(false);
                mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Blutooth_client btSet = mClient;
                        btSet.cancelScan(getApplicationContext());
                    }
                });
            }

            /*기기 찾음*/
            @Override
            public void onFoundDevice(BluetoothDevice bluetoothDevice) {
                addDeviceToArrayAdapter(bluetoothDevice);
                message += "\n" + bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress();
                mLoadingDialog.setMessage(message);
            }

            @Override
            public void onFinish() {
                Log.d("Test", "Scan finish.");
                message = "";
                mLoadingDialog.cancel();
                mLoadingDialog.setCancelable(false);
                mLoadingDialog.setOnCancelListener(null);
                mDeviceListDialog.show();
            }
        });
    }

    /*기기 연결*/
    private void connect(BluetoothDevice device) {
        mLoadingDialog.setMessage("Connecting....");
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
        Blutooth_client btSet =  mClient;
        btSet.connect(getApplicationContext(), device, mBTHandler);
    }

    /*스트리밍 핸들러*/
    private Blutooth_client.BluetoothStreamingHandler mBTHandler = new Blutooth_client.BluetoothStreamingHandler() {
        ByteBuffer mmByteBuffer = ByteBuffer.allocate(1024);

        @Override
        public void onError(Exception e) {
            mLoadingDialog.cancel();
            addText("Messgae : Connection error - " +  e.toString() + "\n");
            //mMenu.getItem(0).setTitle(R.string.action_connect);
        }

        @Override
        public void onDisconnected() {
            //mMenu.getItem(0).setTitle(R.string.action_connect);
            mLoadingDialog.cancel();
            addText("Messgae : Disconnected.\n");
        }
        /*데이터 송신*/
        @Override
        public void onData(byte[] buffer, int length) {
            if(length == 0) return;
            if(mmByteBuffer.position() + length >= mmByteBuffer.capacity()) {//용량이 크면 2배로
                ByteBuffer newBuffer = ByteBuffer.allocate(mmByteBuffer.capacity() * 2);
                newBuffer.put(mmByteBuffer.array(), 0,  mmByteBuffer.position());
                mmByteBuffer = newBuffer;
            }
            mmByteBuffer.put(buffer, 0, length);
            if(buffer[length - 1] == '\0') {//공백까지 읽기
                addText(mClient.getConnectedDevice().getName() + " : " +
                        new String(mmByteBuffer.array(), 0, mmByteBuffer.position()) + '\n');
                mmByteBuffer.clear();
            }
        }

        /*연결되면*/
        @Override
        public void onConnected() {
            addText("Messgae : Connected. " + mClient.getConnectedDevice().getName() + "\n");
            mLoadingDialog.cancel();
            mMenu.getItem(0).setTitle(R.string.action_disconnect);
        }
    };

    /*메세지 전송*/
    public void sendStringData(String data) {
        data += '\0';
        byte[] buffer = data.getBytes();
        if(mBTHandler.write(buffer)) {
            addText("Me : " + data + '\n');
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        mClient.claer();
        Intent finish_intent = new Intent("send_finish");
        sendBroadcast(finish_intent);
        stopService(BT_service_intent);
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    /*메뉴*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean connect = mClient.isConnection();
        if(item.getItemId() == R.id.action_connect) {
            if (!connect) {
                mDeviceListDialog.show();
            } else {
                mBTHandler.close();
            }
            return true;
        } else {
            Intent intent = new Intent(MainActivity.this,Add_number_dialog.class);
            startActivity(intent);
            return true;
        }
    }

    private void showCodeDlg() {
       /* EditText editText = new EditText(this);
        editText.setHint("전화번호");
        new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_DialogWhenLarge)
                .setView(editText)
                .setPositiveButton("OK", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();*/
    }

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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder backkey_dialog = new AlertDialog.Builder(this);

        backkey_dialog.setTitle("종료하시겠습니까? 블루투스 연결이 종료됩니다.\n"
                +"블루투스 연결을 유지하려면 홈키를 이용해 주세요");
        backkey_dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent finish_intent = new Intent("send_finish");
                sendBroadcast(finish_intent);
                stopService(BT_service_intent);
                finish();
            }
        });
        backkey_dialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        backkey_dialog.show();
    }
}

