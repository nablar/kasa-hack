package com.albanrecly.wifipasswordkasa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 0);
        }


        List<ScanResult> apList = manager.getScanResults();
        Tools.setupApList(apList);
        try {
            Tools.lock.await();
        } catch (InterruptedException e) {
            Tools.log(Log.getStackTraceString(e));
        }

        try {
            Method method = manager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(manager, null, true);
        } catch(Exception e){
            Tools.log(Log.getStackTraceString(e));
        }


        WifiInfo info = manager.getConnectionInfo();
        Tools.setMac(info.getMacAddress().toUpperCase());
        Tools.setupJSON();

        UDPListener udpListener = new UDPListener();
        Tools.log("About to start UDP listener");
        udpListener.execute();
    }

    public static void startUDPSender(String IP){
        textView.setText("Received UDP Broadcast. Sending UDP reply\n");
        String ip = IP.split(",")[0], port = IP.split(",")[1];
        new UDPSender().execute(ip, port);
    }

    public static void startTCPServer(){
        textView.append("UDP Broadcast sent. Starting TCP server...\n");
        new TCPServer().execute();
    }

    public static void appendText(String str){
        MainActivity.textView.append(str+"\n");
    }
}
