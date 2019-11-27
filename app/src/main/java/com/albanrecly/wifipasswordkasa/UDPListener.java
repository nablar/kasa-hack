package com.albanrecly.wifipasswordkasa;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;


public class UDPListener extends AsyncTask<String, String, String> {
    public static String SmartPhoneIP = "NOTYET";

    @Override
    protected String doInBackground(String... strings) {
        try {
            DatagramSocket socket = new DatagramSocket(Tools.PORT, InetAddress.getByName(Tools.BROADCAST_IP));
            byte[] recvBuf = new byte[15000];
            //socket.setSoTimeout(1000);
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            Log.e("UDP", "Waiting for UDP broadcast");
            socket.receive(packet);

            String senderIP = packet.getAddress().getHostAddress();
            byte[] data = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
            String message = Tools.hexToAscii(Tools.decode(data));

            Tools.log("Got UDP broadcast from " + senderIP + " on port : "+packet.getPort()+", message: " + message);

            socket.close();
            return senderIP+","+packet.getPort();
        } catch(SocketException e){
            Log.e(Tools.TAG, "Socket error with UDP socket");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }
        catch (IOException e){
            Log.e(Tools.TAG, "IO error with UDP socket");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        MainActivity.startUDPSender(result);
    }
}

