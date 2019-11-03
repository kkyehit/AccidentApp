package com.example.accident_alarm;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by 희준 on 2016-06-27.
 */
public class Search_Service extends Service {
    /*notification*/
    Notification notification;
    NotificationManager notificationManager;
    PendingIntent pendingIntent;
    Intent intent;
    int noti_flag = 0;

    Intent start_alarm;

    /*위치 관련*/
    Calendar calendar;

    /*경도, 위도*/
    static Double lattitude = new Double(999);
    static Double longitude = new Double(999);
    Location start_location;

    /*권한설정 android 23 이상*/
    String[] Permission_List = {android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.INTERNET};

    /*sensor listener*/
    SensorEventListener senser_event_Listener = new SensorEventListener() {

        int iorientation = -1;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (iorientation < 0) {
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

    /*서버 관련*/
    Handler post_handler;//

    BufferedWriter bufferedWriter;//서버에 보낼떄
    BufferedReader bufferedReader;//서버에서 받을 떄

    private String ip = "192.168.1.2";// ip
    private int Port = 9999; // prot

    Socket socket;//소캣, 통신

    int Thread_flag = 1;

    String send_msg, Receive_msg;

    /*list에 정보 업데이트*/
    private Runnable list_Update = new Runnable() {
        public void run() {
            //Toast.makeText(getBaseContext(), "Sever : " + Receive_msg, Toast.LENGTH_LONG).show();
            Intent intent;
            if(Receive_msg.charAt(0) == 'T'){
                intent = new Intent("send_bt_1");
                sendBroadcast(intent);
            }else {
                intent = new Intent("send_bt_2");
                sendBroadcast(intent);
            }

        }
    };


    /*notification 정보 업데이트*/
    private Runnable notify_Update = new Runnable() {
        public void run() {
            notification = null;
           notification_change();
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
    Thread server_thread = new Thread() {
        Double temp_lattitude = new Double(999),
                temp_longitude = new Double(999);

        @Override
        public void run() {
            super.run();
            int i = 0;
            while (Thread_flag == 1) {
                if (((!lattitude.equals(temp_lattitude))
                        && (!longitude.equals(temp_longitude)))) {
                    temp_longitude = longitude;
                    temp_lattitude = lattitude;
                    calendar = new GregorianCalendar();
                    post_handler.post(notify_Update);
                    try {
                        try {//소캣 설정
                                setSocket(ip, Port);
                        } catch (IOException e) {
                        }
                        while (true) {
                            Receive_msg = bufferedReader.readLine();
                            if (Receive_msg.compareTo("ONLINE") == 0) {
                                PrintWriter out = new PrintWriter(bufferedWriter, true);
                                send_msg = "4/" + calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.HOUR) + ":"
                                        + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "/"
                                        + "Latting" + "/" + lattitude + "/" + longitude + "/";
                                out.println(send_msg);
                                while ((Receive_msg.compareTo("ONLINE") == 0) || (Receive_msg == null)) {
                                    Receive_msg = bufferedReader.readLine();
                                    Log.d("Chatting running", "chatting running");
                                }

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
                    i=0;
                }
            }
        }
    };

    IntentFilter intentFilter = new IntentFilter();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("send_broad")) {
               noti_flag = 1;
                notification = null;
                notification_change();
            } else if (intent.getAction().equals("send_finish")) {
                noti_flag = 0;
                notification = null;
                notification_change();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilter.addAction("send_broad");
        intentFilter.addAction("send_finish");
        registerReceiver(broadcastReceiver,intentFilter);


        Thread_flag = 1;
        calendar = new GregorianCalendar();
        post_handler = new Handler();
        LocationManager L_M = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        My_Location_Listener L_L = new My_Location_Listener();
                /*권한 설정*/
//        if(Build.VERSION.SDK_INT>21) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "설정을 확인하고 다시 시작 해 주세요", Toast.LENGTH_LONG).show();
            onDestroy();
        }
    //}
        L_M.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, L_L);
        server_thread.start();

        start_alarm = new Intent(Search_Service.this, alarm.class);
        start_alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        intent = new Intent(Search_Service.this , MainActivity.class);
        pendingIntent = PendingIntent.getActivities(Search_Service.this , 0,
                new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        notification_change();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Thread_flag = 0;
        //Toast.makeText(getBaseContext(),"destriy service",Toast.LENGTH_LONG).show();
        notificationManager.cancel(7777);
    }

    public void notification_change(){
        if(noti_flag == 0) {
            notification = new Notification.Builder(getApplicationContext())
                .setContentIntent(pendingIntent)
                .setContentTitle("alarm")
                .setContentText("Lacation : "+ lattitude + "/" + longitude +"\n     " +
                        calendar.get(Calendar.HOUR) +" : " + calendar.get(Calendar.MINUTE))
                .setSmallIcon(R.drawable.bt_disable_icon)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
            notificationManager.notify(7777,notification);
        }else if(noti_flag == 1) {
            notification = new Notification.Builder(getApplicationContext())
                    .setContentIntent(pendingIntent)
                    .setContentTitle("alarm")
                    .setContentText("Lacation : "+ lattitude + "/" + longitude +"\n     " +
                            calendar.get(Calendar.HOUR) +" : " + calendar.get(Calendar.MINUTE))
                    .setSmallIcon(R.drawable.bt_connect_icon)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .build();
            notificationManager.notify(7777,notification);
        }
    }
}
