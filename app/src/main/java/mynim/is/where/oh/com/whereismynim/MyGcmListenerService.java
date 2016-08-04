package mynim.is.where.oh.com.whereismynim;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MyGcmListenerService extends GcmListenerService {

    public static int MSG_DEF = 2016010132, MSG_CHT = 201701920;
    private static String batStatus = "Unknown";
    private static final String TAG = "MyGcmListenerService";

    private SQLiteDatabase database;
    private String dbName = "WMN_DB";
    private String createTable =
            "create table if not exists WMN_CHAT(" +
                    "`id` integer primary key autoincrement, " +
                    "`from` integer, " +
                    "`to` integer, " +
                    "`msg` text, " +
                    "`date` datetime, " +
                    "`read` integer);";

    private void sendMessage(int from, int to) {
        Intent intent = new Intent("WMN_CHAT_EVENT");
        intent.putExtra("froma", from);
        intent.putExtra("toa", to);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + data.getString("title"));

        final SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = prefs.edit();

        if(data.getString("title").equals("unlock_alarm")){
            prefEditor.putBoolean("alarm", true);
            prefEditor.commit();
        }

        if(data.getString("title").equals("lock_alarm")){
            prefEditor.putBoolean("alarm", false);
            prefEditor.commit();
        }


        if(data.getString("title").equals("#FORCELOC#")) {
            if(data.getString("message")==null && !prefs.getBoolean("force", true)) return;
            sendNotification("내님은 어디에", "상대방이 위치정보를 조회했습니다.", data.getInt("idx"));
            //HandlerThread handlerThread = new HandlerThread("GCM");
            //handlerThread.start();
            if(Looper.myLooper()==null) Looper.prepare();
            LocationService loc = LocationService.getLocationManager(this);
            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("id", Integer.toString(prefs.getInt("user_key", 0)));
            mapData.put("lang", Double.toString(loc.latitude));
            mapData.put("long", Double.toString(loc.longitude));
            mapData.put("mode", Integer.toString(1));
            Log.e("LOC_UP", "Message force location sent");
            new Communicator().postHttp(AddInfo.URL_UPDATE_LOC, mapData, new Handler() {});
        }else if(data.getString("title").equals("LOC_UP")) {
            Intent intent = new Intent("LOC_UP");
            Log.e("LOC_UP", "Message Locaton updated sent");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }else if(data.getString("title").equals("BAT_UP_R")) {
            Intent intent = new Intent("BAT_UP_R");
            intent.putExtra("batStatus", data.getString("message"));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }else if(data.getString("title").equals("BAT_UP_D")){
            Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batStatus = Float.toString(((float)level / (float)scale) * 100.0f);
            if(Looper.myLooper()==null) Looper.prepare();
            HashMap<String, String> mapData = new HashMap<>();
            mapData.put("id", Integer.toString(prefs.getInt("part_key", 0)));
            mapData.put("title", "BAT_UP_R");
            mapData.put("message", batStatus);
            new Communicator().postHttp(AddInfo.URL_SEND, mapData, new Handler() {
            });
        }else if(data.getString("title").equals("MSG_CALL")) {
            createDatabase();
            createTable();
            insertData(Integer.parseInt(data.getString("froma").trim()), Integer.parseInt(data.getString("toa").trim()), data.getString("message"));
            if(MainActivity.isRun != true) sendNotification("내님은 어디에", "메세지가 도착했습니다", MSG_CHT);
            sendMessage(Integer.parseInt(data.getString("froma").trim()), Integer.parseInt(data.getString("toa").trim()));

        }else sendNotification(data.getString("title"), data.getString("message"), MSG_DEF);
    }

    private void sendNotification(String from, String message,int idx) {
        final SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = prefs.edit();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, IntroActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if(!prefs.getBoolean("notiacc", true)) return;

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(from)
                    .setContentText(message)
                    .setTicker(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(idx, notificationBuilder.build());
    }

    public void createDatabase(){
        database = openOrCreateDatabase(dbName, android.content.Context.MODE_PRIVATE, null);
    }

    public void createTable(){
        try{
            database.execSQL(createTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertData(int from, int to, String msg){
        database.beginTransaction();
        try{
            String sql = "insert into WMN_CHAT(`from`, `to`, `msg`, `date`, read) values (" + from + ", " + to + ", '" + msg + "', datetime('now', 'localtime') ," + 0 + ");";
            database.execSQL(sql);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }


}
