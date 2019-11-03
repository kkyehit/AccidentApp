package com.example.accident_alarm;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by 희준 on 2016-05-03.
 */
/*
사고 상황 액티비티
 */
public class Accident extends AppCompatActivity {
    /* UI 설정 */
    ListView listView;
    TextView massage;
    Button solve;
    ImageButton emergency_call;

    /*Intent, 값 가져오기 */
    Intent intent;

    /*google map */
    GoogleMap _Map;
    MarkerOptions current_point;
    SensorManager sensor_Manager;
    Double latitude, longitude;

    /*사고 정보 리스트 */
    Accident_List_Setting accident_list_setting = new Accident_List_Setting();
    int situation;

    boolean Compass_Enable;

    /*sensor listener 지도에 사용 */
    SensorEventListener senser_event_Listener = new SensorEventListener(){
        //초기화
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

    /**********
     *서버 관련*
     ***********/
    Handler post_handler;//

    Calendar calendar;
    int date, hour, min, sec;

    BufferedWriter bufferedWriter;//서버에 보낼떄
    BufferedReader bufferedReader;//서버에서 받을 떄

    private String ip = "192.168.1.2"; // ip
    private int Port = 9999; // prot

    Socket socket;//소캣, 통신

    int Thread_flag = 1;

    String send_msg, Receive_msg;

    /*list에 정보 업데이트*/
    private Runnable list_Update = new Runnable() {
        public void run() {
      //      Toast.makeText(getBaseContext(), "Sever : " + Receive_msg, Toast.LENGTH_LONG).show();
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
    Thread singo_thread = new Thread(){
        @Override
        public void run() {
            super.run();
            try {
                try {//소캣 설정
                    setSocket(ip, Port);
                } catch (IOException e) {
                }
                while (true) {
                    Receive_msg = bufferedReader.readLine();

                    if (Receive_msg.compareTo("ONLINE") == 0) {// 서버에서 온라인이라는 신호를 받으면
                        PrintWriter out = new PrintWriter(bufferedWriter, true);

                        /*메세지 보냄*/
                        send_msg = situation + "/" + date + "/" + hour + ":" + min + ":" + sec + "/"
                                + "Latting" + "/"+latitude+ "/" +longitude+"/";
                        out.println(send_msg);

                        /*다음 메세지를 받음*/
                        while ((Receive_msg.compareTo("ONLINE") == 0) || (Receive_msg == null)) {
                            Receive_msg = bufferedReader.readLine();
                            Log.d("Chatting running", "chatting running");
                        }
                        out.println("#33");//종료한다는 신호를 보냄
                        break;
                    }
                }
                Log.d("Socket close", "complet socket close");
                socket.close();
            } catch (Exception e) {
                Log.d("error,", "[" + e + ")");
            }
        }
    };

    Thread solve_thread = new Thread(){
        @Override
        public void run() {
            super.run();
            try {
                try {//소캣 설정
                    setSocket(ip, Port);
                } catch (IOException e) {
                }
                while (true) {
                    Receive_msg = bufferedReader.readLine();
                    if (Receive_msg.compareTo("ONLINE") == 0) {// 서버에서 온라인이라는 신호를 받으면

                        PrintWriter out = new PrintWriter(bufferedWriter, true);

                        /*메세지 보냄*/
                        send_msg = 3 + "/" + date + "/" + hour + ":" + min + ":" + sec + "/"
                                + "Latting" + "/"+latitude+ "/" +longitude+"/";
                        out.println(send_msg);

                        /*다음 메세지를 받음*/
                        while ((Receive_msg.compareTo("ONLINE") == 0) || (Receive_msg == null)) {
                            Receive_msg = bufferedReader.readLine();
                            Log.d("Chatting running", "chatting running");
                        }

                        out.println("#33");//종료한다는 신호를 보냄
                        break;
                    }
                }
                Log.d("Socket close", "complet socket close");
                socket.close();
            } catch (Exception e) {
                Log.d("error,", "[" + e + ")");
            }
        }
    };

    /**/
   static Emergency_call_adapter E_call;
    View view;
   Activity activity;
   static ListView dialog_list;

    FileInputStream input_open;
    FileOutputStream output_open;
    String s;
    byte[] data = null;
    String string_list = "";
    File file;
    String[] parsed_string_list;
    String temp_s = "";

    String[] Permission_List = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET};


    public class My_Location_Listener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {// 위치가 변화면
            /*현제위치 받기*/
                finish();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accident);
        /* 맵핑 과정 */
        _Map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.accident_map)).getMap();
        sensor_Manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /*Extra 값 받기위함*/
        intent = getIntent();

        /*경도 위도 설정*/
        latitude = intent.getDoubleExtra("Lattitude",0);
        longitude = intent.getDoubleExtra("Longitude",0);
        LatLng Accident_point = new LatLng(latitude,longitude);
        _Map.animateCamera(CameraUpdateFactory.newLatLngZoom(Accident_point,30));

        /*현제위치 표시*/
        current_point = new MarkerOptions();
        current_point.position(Accident_point);//위치설정
        current_point.title("alarm");//타이틀 설정
        current_point.snippet("Snippet");//내용설정
        current_point.icon(BitmapDescriptorFactory.fromResource(R.drawable.carefull));//아이콘 설정
        _Map.addMarker(current_point).showInfoWindow();//보여주기

        /*어댑터 설정*/
        ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,accident_list_setting.get_array_list());

        /* 리스트뷰 설정*/
        listView = (ListView) findViewById(R.id.near_list);
        listView.setAdapter(arrayAdapter);
        listView.setDividerHeight(2);
        listView.setBottom(5);

        /*mapping*/
        massage = (TextView) findViewById(R.id.accident_text);
        emergency_call = (ImageButton) findViewById(R.id.emergency_call);
        solve = (Button) findViewById(R.id._solve);

        /*현재위치와 사고인지 정차인지 알려줌*/
        situation = intent.getIntExtra("Extra",0);
        massage.setText("현제위치\n 경도 : " + intent.getDoubleExtra("Lattitude",0)+
                "\n 위도 : " + intent.getDoubleExtra("Longitude",0));
        if(situation == 1)
            massage.setText(massage.getText()+"\n사고상황입니다.");
        else if(situation == 2)
            massage.setText(massage.getText()+"\n정차상황입니다.");

        calendar = new GregorianCalendar();
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR);
        min = calendar.get(Calendar.MINUTE);
        sec = calendar.get(Calendar.SECOND);
        singo_thread.start();
        String dirPath = Environment.getExternalStorageDirectory()+"/huijun";


        activity = this;
        E_call = new Emergency_call_adapter(activity);

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
            parsed_string_list = string_list.split("/");

            for(int i = 0; i< parsed_string_list.length;i++) {
                E_call.setName(parsed_string_list[i]);
            }

        }catch (Exception e){

        }

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

        L_M.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, L_L);



        emergency_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                view = activity.getLayoutInflater().inflate(R.layout.emergency_call_list, null);
                // 해당 뷰에 리스트뷰 호출
                dialog_list = (ListView)view.findViewById(R.id.call_list);
                // 리스트뷰에 어뎁터 설정
                dialog_list.setAdapter(E_call);

                // 다이얼로그 생성
                AlertDialog.Builder listViewDialog = new AlertDialog.Builder(activity);
                // 리스트뷰 설정된 레이아웃
                listViewDialog.setView(view);
                // 확인버튼
                listViewDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            for(int i = 0 ; i < E_call.getCount();i++){
                                temp_s += E_call.getItem(i)+"/";
                            }
                            FileOutputStream Save_file = openFileOutput("number.txt", MODE_PRIVATE);
                            OutputStreamWriter Save_file_writer = new OutputStreamWriter(Save_file);
                            Save_file_writer.write(temp_s);
                            Save_file_writer.close();
                            Save_file.close();
                            Toast.makeText(getApplication(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        } catch (java.io.IOException e2) {
                            Toast.makeText(getApplication(), "오류가 발생하였습니다." +
                                    " 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // 아이콘 설정
                listViewDialog.setIcon(R.drawable.emergency_call);
                // 타이틀
                listViewDialog.setTitle("Emergency_call");
                // 다이얼로그 보기
                listViewDialog.show();

            }
        });
        solve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        current_point = null;
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        current_point = null;


    }

    @Override
    protected void onStop() {
        super.onStop();
        current_point = null;
        solve_thread.start();
    }

    public class Emergency_call_adapter extends BaseAdapter {
        ArrayList<String> list;
        Activity activity;


        Emergency_call_adapter(Activity activity){
            this.activity = activity;
            list = new ArrayList<String>();

        }

        // 리스트에 값을 추가할 메소드
        public void setName(String name)
        {
            list.add(name);
        }
        @Override
        public int getCount() {
            // 리스트뷰 갯수 리턴
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // 리스트 값 리턴
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder    holder  = null;
            final int pos = position;
            TextView name;

            // 최초 뷰 생성
            if(convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.emergency_call_text, parent, false);
                name    = (TextView) convertView.findViewById(R.id.call_row);

                holder = new ListViewHolder();
                holder.name = name;

                // list values save
                convertView.setTag(holder);
                // 텍스트 보이기
                name.setVisibility(View.VISIBLE);
            }
            else
            {
                // list values get
                holder = (ListViewHolder) convertView.getTag();
                name = holder.name;
            }

            // 리스트 이름 보이기
            name.setText(list.get(pos));

            // 리스트 아이템을 터치 했을 때 이벤트 발생
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent call_intent = new Intent(Intent.ACTION_DIAL);
                    call_intent.setData(Uri.parse("tel:"+list.get(pos)));
                    startActivity(call_intent);
                }
            });

            // 리스트 아이템을 길게 터치 했을 떄 이벤트 발생
            convertView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    list.remove(pos);
                    Accident.dialog_list.clearChoices();
                    Accident.E_call.notifyDataSetChanged();
                    return false;
                }
            });

            return convertView;
        }

        // list values class
        private class ListViewHolder {
            TextView name;
        }
    }


}
