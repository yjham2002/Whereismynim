package mynim.is.where.oh.com.whereismynim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

public class AlarmReceiver extends BroadcastReceiver{
    public void uploadLoc(Context context){
        final SharedPreferences prefs = context.getSharedPreferences("WMN_PREF", context.MODE_PRIVATE);
        if(Looper.myLooper()==null) Looper.prepare();
        LocationService loc = LocationService.getLocationManager(context);
        HashMap<String, String> mapData = new HashMap<>();
        mapData.put("id", Integer.toString(prefs.getInt("user_key", 0)));
        mapData.put("lang", Double.toString(loc.latitude));
        mapData.put("long", Double.toString(loc.longitude));
        mapData.put("mode", Integer.toString(1));
        Log.e("LOC_UP", "Message force location sent");
        new Communicator().postHttp(AddInfo.URL_UPDATE_LOC, mapData, new Handler() {
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("WMN_ALARM", "Ring");
        final SharedPreferences prefs = context.getSharedPreferences("WMN_PREF", context.MODE_PRIVATE);
        if(prefs.getBoolean("alarm", true)) uploadLoc(context);
    }
}