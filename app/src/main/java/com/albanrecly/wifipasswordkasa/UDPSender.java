package com.albanrecly.wifipasswordkasa;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPSender extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... strings) {
        try {
            Tools.log("About to send UDP packet");
            InetAddress IP = InetAddress.getByName(strings[0]);
            int port = Integer.parseInt(strings[1]);

            int msg_length = Tools.JSONanswerUDP.length();
            byte[] message = Tools.encode(Tools.JSONanswerUDP.getBytes());

            DatagramSocket s = new DatagramSocket(Tools.PORT);
            DatagramPacket p = new DatagramPacket(message, msg_length, IP, port);
            s.send(p);
            Tools.log("UDP Packet sent to "+IP+":"+port);
            s.close();
        }catch(UnknownHostException e){
            Log.e(Tools.TAG, "Unkown host");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }catch(SocketException e) {
            Log.e(Tools.TAG, "Problem with Socket");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        } catch(IOException e){
            Log.e(Tools.TAG, "Problem while sending packet");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result){
        MainActivity.startTCPServer();
    }
}
