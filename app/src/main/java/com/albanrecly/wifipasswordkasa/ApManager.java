package com.albanrecly.wifipasswordkasa;

import android.content.*;
import android.net.wifi.*;
import java.lang.reflect.*;
import java.util.concurrent.CountDownLatch;

public class ApManager {
    public static CountDownLatch lock = new CountDownLatch(1);
    //check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    // toggle wifi hotspot on or off
    public static boolean configApState(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if(isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, !isApOn(context));
            lock.countDown();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        lock.countDown();
        return false;
    }
} // end of class