package com.albanrecly.wifipasswordkasa;

import android.net.wifi.ScanResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Tools {
    public static CountDownLatch lock = new CountDownLatch(1);

    public static final int PORT = 9999;
    public static final String BROADCAST_IP = "255.255.255.255";
    public static final String TAG = "KASA_HACK";
    private static String MAC = "";
    public static String JSONanswerUDP;
    public static String JSONanswerTCP;
    public static String JSONanswerTCPCloud;
    public static String JSONanswerTCPTimezone;
    private static JSONArray apList;

    public static void setMac(String str){
        MAC = str;
    }

    public static String getMac(){
        return MAC;
    }


    public static void log(String msg){
        Log.d(TAG, msg);
    }

    public static byte[] decode(byte[] bArr){
        if (bArr != null && bArr.length > 0 ){
            int i = -85;
            for(int i2 = 0 ; i2 < bArr.length ; i2++){
                byte b = (byte) (i^bArr[i2]);
                i = bArr[i2];
                bArr[i2] = b;
            }
        }
        return bArr;
    }

    public static byte[] encode(byte[] bArr){
        int i = -85;
        for (int i2 = 0; i2<bArr.length;i2++){
            bArr[i2] = (byte) (i^bArr[i2]);
            i = bArr[i2];
        }
        return bArr;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hexToAscii(byte[] bArr){
        String hexStr = bytesToHex(bArr);
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static void setupJSON(){
        try {
            /* UDP */
            JSONObject ans = new JSONObject(); // Outside (sytem)
            JSONObject ans1 = new JSONObject(); // Middle (get_sysinfo)
            JSONObject ans2 = new JSONObject(); // Inside (all the information)

            ans2.put("err_code", 0);
            ans2.put("sw_ver", "1.2.5 Build 171206 Rel.085954");
            ans2.put("hw_ver", "1.0");
            ans2.put("type", "IOT.SMARTPLUGSWITCH");
            ans2.put("model", "HS110(US)");
            ans2.put("mac", Tools.getMac());
//            ans2.put("mac", "50:C7:BF:74:95:E4");
            ans2.put("deviceId", "800699C9481323714E4C141E0B240C141879D549");
            ans2.put("hwId", "60FF6B258734EA6880E186F8C96DDC61");
            ans2.put("fwId", "00000000000000000000000000000000");
            ans2.put("oemId", "FFF22CFF774A0B89F7624BFC6F50D5DE");
            ans2.put("alias", "");
            ans2.put("dev_name", "Wi-Fi Smart Plug With Energy Monitoring");
            ans2.put("icon_hash", "");
            ans2.put("relay_state", 1);
            ans2.put("on_time", 0);
            ans2.put("active_mode", "");
            ans2.put("feature", "TIM:ENE");
            ans2.put("updating", 0);
            ans2.put("led_off", 0);
            ans2.put("latitude", 45.498173);
            ans2.put("longitude", -73.617038);

            ans1.put("get_sysinfo", ans2);

            ans.put("system", ans1);

            JSONanswerUDP = ans.toString();

            /* TCP */
            ans = new JSONObject(); // Outside (sytem)
            ans1 = new JSONObject(); // Middle (get_sysinfo)
            ans2 = new JSONObject(); // Inside (all the information)

            ans2.put("ap_list", Tools.apList);
            ans2.put("err_code",0);
            ans1.put("get_scaninfo", ans2);
            JSONObject source = new JSONObject();
            source.put("source", "46a4d58b-6279-432c-ae23-e115c2db8354");
//            ans.put("context", source);
            ans.put("netif", ans1);

            JSONanswerTCP = ans.toString();

            ans = new JSONObject(); // Outside (sytem)
            ans1 = new JSONObject(); // Middle (get_sysinfo)
            ans2 = new JSONObject(); // Inside (all the information)

            ans2.put("username","alban.recly@polymtl.ca");
            ans2.put("server","devs.tplinkcloud.com");
            ans2.put("binded",1);
            ans2.put("cld_connection",0);
            ans2.put("illegalType",-1);
            ans2.put("stopConnect",-1);
            ans2.put("tcspStatus",-1);
            ans2.put("fwDlPage","");
            ans2.put("tcspInfo","");
            ans2.put("fwNotifyType",0);
            ans2.put("err_code",0);
            ans1.put("get_info", ans2);
            ans.put("cnCloud", ans1);
//            ans.put("context", source);

            JSONanswerTCPCloud = ans.toString();
            ans = new JSONObject();
            ans1 = new JSONObject();
            ans2 = new JSONObject();
            ans2.put("err_code",0);
            ans1.put("set_timezone", ans2);
            ans.put("time", ans1);

            JSONanswerTCPTimezone = ans.toString();
        } catch(JSONException e){
            Log.e(Tools.TAG, "Problem while creating JSON object");
            Log.e(Tools.TAG, Log.getStackTraceString(e));
        }
    }

    public static byte[] getPadding(int length){
        byte[] padding = new byte[4];
        padding[3] = (byte) (length & 0xFF);
        padding[2] = (byte) ((length >> 8) & 0xFF);
        padding[1] = (byte) ((length >> 16) & 0xFF);
        padding[0] = (byte) ((length >> 24) & 0xFF);
        return padding;
    }

    public static void setupApList(List<ScanResult> apList){
        try {
            Tools.apList = new JSONArray();
            for (ScanResult sr : apList) {
                Tools.log(sr.toString());
                JSONObject ap = new JSONObject();
                ap.put("ssid", sr.SSID);
                if(sr.capabilities.contains("WPS")){
                    ap.put("key_type", 3);
                } else if(sr.capabilities.contains("WPA")){
                    ap.put("key_type", 2);
                } else{
                    ap.put("key_type", 0);
                }
                Tools.apList.put(ap);
            }
            Tools.log(Tools.apList.toString());
        } catch (JSONException e){
            Tools.log("Error with JSON");
            Tools.log(Log.getStackTraceString(e));
        }
        Tools.lock.countDown();
    }
}
