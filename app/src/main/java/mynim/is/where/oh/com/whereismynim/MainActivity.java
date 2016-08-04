package mynim.is.where.oh.com.whereismynim;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    public static AlarmManager mAlarmMgr;
    private static final LatLng SEOUL = new LatLng(37.6, 127);
    public static LatLng now, nowYou;
    public static double distance = 0.0f;
    private GoogleMap map;
    private Marker nowMark;
    public static ArrayList<LocationInfo> partLoc;

    private TextView user_name_pane, user_id_pane, current_pane, Couple_me, Couple_you, batStatus, distanceStat;
    private ImageView refresh, profile, director,circlepic;
    private FrameLayout titlebar, parentView;
    private Button bt_profile, bt_setting, bt_logout, bt_mag;

    // SCROLL VIEW RELATED DEC START
    private LinearLayout scroller;
    private FrameLayout parallex;
    private ParallaxScrollView pscroll;
    // SCROLL VIEW RELATED DEC END

    private DrawerArrowDrawable drawerArrowDrawable;
    private float offset;
    private boolean flipped;

    private AnimationSet animset;

    public static int user_key = 0, part_key = 0;
    public static String user_name = "Loading...", part_name = "Loading...", user_id = "Loading...", user_token;

    public static String currentInfo = "최근 위치 정보 업데이트 : 정보 없음";

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLoc();
                } else {
                    currentInfo = "최근 위치 정보 업데이트 : 업데이트 실패";
                    current_pane.setText(currentInfo);
                }
                return;
            }
            default: break;
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.profile_pic:
                if(user_id.equals("yjham2002@gmail.com")){
                    Intent iia = new Intent(this, AdminActivity.class);
                    startActivity(iia);
                }
                break;
            case R.id.bt_profile:
                Intent ii = new Intent(this, ProfileActivity.class);
                startActivity(ii);
                break;
            case R.id.bt_setting:
                Intent aa = new Intent(this, SettingActivity.class);
                startActivity(aa);
                break;
            case R.id.bt_logout:
                SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putInt("auto_login", 0);
                prefEditor.putBoolean("notiacc", true);
                prefEditor.putBoolean("force", true);
                prefEditor.putBoolean("alarm", true);
                prefEditor.commit();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                finish();
                break;
            case R.id.bt_magnify:
                String addressStr = "";
                try {
                    Geocoder myLocation = new Geocoder(this, Locale.getDefault());
                    List<Address> myList = myLocation.getFromLocation(nowYou.latitude, nowYou.longitude, 1);
                    Address address = myList.get(0);
                    addressStr += address.getAddressLine(0);
                }catch(Exception e){
                    addressStr = "정확하지 않은 위치";
                }

                Intent i2 = new Intent(this, mapActivity.class);
                i2.putExtra("updateInfo", current_pane.getText().toString());
                i2.putExtra("geoInfo", addressStr);
                startActivity(i2);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                break;
            case R.id.refresh:
                refresh.startAnimation(animset);
                new Communicator().getHttp(AddInfo.URL_FORCE_LOC + part_key, new Handler() {});
                break;

            default: break;
        }
    }

    public void getPartnerLoc(){
        if(partLoc == null) partLoc = new ArrayList<>();
        HashMap<String, String> mapData = new HashMap<>();
        mapData.put("id", Integer.toString(MainActivity.part_key));
        new Communicator().postHttp(AddInfo.URL_GET_LOC, mapData, new Handler(){
            @Override
            public void handleMessage(Message msg){
                String jsonString = msg.getData().getString("jsonString");
                partLoc.clear();
                PolylineOptions partPath = new PolylineOptions().color(getResources().getColor(R.color.colorAccent)).width(5.0f);
                try {
                    JSONArray json_arr = new JSONArray(jsonString);
                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject json_list = json_arr.getJSONObject(i);
                        partLoc.add(new LocationInfo(new LatLng(json_list.getDouble("lang"), json_list.getDouble("long")), json_list.getString("dates")));
                        partPath.add(partLoc.get(i).loc);
                        Marker temp = map.addMarker(new MarkerOptions().position(partLoc.get(i).loc).title(partLoc.get(i).date).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        nowYou = temp.getPosition();
                    }
                    map.addPolyline(partPath);

                } catch (JSONException e) {
                    nowYou = SEOUL;
                    e.printStackTrace();
                } finally {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(nowYou, 15));
                    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    distance = util.MapUtils.distBetween(now, nowYou);
                    distanceStat.setText(Double.toString(distance) + "km");
                }

            }
        });
    }

    public void updateLoc(){
        map.clear();
        LocationService loc = LocationService.getLocationManager(this);
        now = new LatLng(loc.latitude, loc.longitude);
        if(now.latitude == 0.0 && now.longitude == 0.0){
            showToast("상대방의 위치 정보를 불러오는 중 오류가 발생하였습니다.");
            return;
        }
        nowMark = map.addMarker(new MarkerOptions().position(now).title("현 위치"));
        if(!loc.checkPermission(this)) {
            now = SEOUL;
            Toast.makeText(getApplicationContext(), "위치 정보 이용에 동의하세요", Toast.LENGTH_LONG).show();
        }
        HashMap<String, String> mapData = new HashMap<>();
        mapData.put("id", Integer.toString(user_key));
        mapData.put("lang", Double.toString(now.latitude));
        mapData.put("long", Double.toString(now.longitude));
        mapData.put("mode", Integer.toString(0));
        new Communicator().postHttp(AddInfo.URL_UPDATE_LOC, mapData, new Handler(){
            @Override
            public void handleMessage(Message msg){
                map.getUiSettings().setAllGesturesEnabled(false);
                long nowT = System.currentTimeMillis();
                Date date = new Date(nowT);
                SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String strCurDate = CurDateFormat.format(date);
                currentInfo = "최근 위치 정보 업데이트 : " + strCurDate;
                current_pane.setText(currentInfo);
                getPartnerLoc();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();

        prefEditor.putInt("auto_login", 1);
        prefEditor.commit();

        circlepic = (ImageView)findViewById(R.id.profile_pic);
        circlepic.setOnClickListener(this);

        Couple_me = (TextView)findViewById(R.id.Me);
        Couple_you = (TextView)findViewById(R.id.You);
        distanceStat = (TextView)findViewById(R.id.Distance);

        user_name_pane = (TextView)findViewById(R.id.user_name);
        user_id_pane = (TextView)findViewById(R.id.user_id);
        current_pane = (TextView)findViewById(R.id.current_info);
        titlebar = (FrameLayout)findViewById(R.id.titlebar);
        refresh = (ImageView)findViewById(R.id.refresh);
        profile = (ImageView)findViewById(R.id.profile);
        director = (ImageView)findViewById(R.id.dirin);
        batStatus = (TextView)findViewById(R.id.batStatus);

        bt_profile = (Button)findViewById(R.id.bt_profile);
        bt_setting = (Button)findViewById(R.id.bt_setting);
        bt_logout = (Button)findViewById(R.id.bt_logout);
        bt_mag = (Button)findViewById(R.id.bt_magnify);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        Animation twink = AnimationUtils.loadAnimation(this, R.anim.twinkle);
        director.startAnimation(twink);

        user_name_pane.setText(user_name);
        Couple_me.setText(user_name);

        getPartnerLoc();

        Couple_you.setText(part_name);
        user_id_pane.setText(user_id);

        refresh.setOnClickListener(this);
        refresh.setDrawingCacheEnabled(true);
        bt_profile.setOnClickListener(this);
        bt_setting.setOnClickListener(this);
        bt_logout.setOnClickListener(this);
        bt_mag.setOnClickListener(this);

        animset = new AnimationSet(false);
        Animation anim1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
        animset.addAnimation(anim1);

        updateLoc();

        AdView mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("FE4EB46DF11F124494E4B402287CE845").build();
        mAdView.loadAd(adRequest);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ImageView imageView = (ImageView) findViewById(R.id.drawer_indicator);
        final Resources resources = getResources();
        drawerArrowDrawable = new DrawerArrowDrawable(resources);
        drawerArrowDrawable.setStrokeColor(resources.getColor(R.color.white));
        imageView.setImageDrawable(drawerArrowDrawable);
        drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                offset = slideOffset;

                // Sometimes slideOffset ends up so close to but not quite 1 or 0.
                if (slideOffset >= .995) {
                    flipped = true;
                    drawerArrowDrawable.setFlip(flipped);
                } else if (slideOffset <= .005) {
                    flipped = false;
                    drawerArrowDrawable.setFlip(flipped);
                }

                drawerArrowDrawable.setParameter(offset);
            }
        });
// gravity compat added
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        // SCROLL VIEW RELATED DEC
        scroller = (LinearLayout)findViewById(R.id.scroller);
        parallex = (FrameLayout)findViewById(R.id.parallex);
        parentView = (FrameLayout)findViewById(R.id.parentView);
        pscroll = (ParallaxScrollView)findViewById(R.id.pscroll);
        // SCROLL VIEW RELATED DEC

        HashMap<String, String> mapData = new HashMap<>();
        mapData.put("id", Integer.toString(part_key));
        mapData.put("title", "BAT_UP_D");
        new Communicator().postHttp(AddInfo.URL_SEND, mapData, new Handler() {
        });

        new SizeS().execute();

    }

    public void onAlarmStart() {
        mAlarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
        final SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        //long cycleTime = prefs.getInt("ctime", 3000); // testing
        long cycleTime = prefs.getInt("ctime", 3) * 3600 * 1000;
        long startTime = SystemClock.elapsedRealtime() + cycleTime;
        Log.e("WMN_ALARM", "Set startTime : " + startTime + ", cycleTime : " + cycleTime);
        mAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, cycleTime, pIntent);
    }

    public void onAlarmStop() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        mAlarmMgr.cancel(pIntent);
    }

    private class SizeS extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(parentView.getHeight() > scroller.getHeight()) scroller.getLayoutParams().height = parentView.getHeight();
            if(parentView.getHeight() > parallex.getHeight()) parallex.getLayoutParams().height = parentView.getHeight();
            //scroller.setPadding(0, 0, 0, parentView.getHeight() > scroller.getHeight() ? parentView.getHeight() - scroller.getHeight() : 0);
            //parallex.setPadding(0, 0, 0, parentView.getHeight() > parallex.getHeight() ? parentView.getHeight() - parallex.getHeight() : 0);
            pscroll.setTopBound(parentView.getHeight());
            super.onPostExecute(result);
        }
    }

    public boolean mFlag;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == 0) mFlag=false;
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            if(!mFlag) {
                Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                mFlag = true;
                mHandler.sendEmptyMessageDelayed(0, 2000);
                return false;
            } else {
                finish();
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        if(prefs.getBoolean("alarm", true)){
            //onAlarmStop();
            onAlarmStart();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("LOC_UP"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("BAT_UP_R"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("LOC_UP")){
                updateLoc();
            }else if(intent.getAction().equals("BAT_UP_R")){
                batStatus.setText("상대방 배터리 잔여량 : " + intent.getExtras().getString("batStatus", "Unknown") + "%");
            }
        }
    };

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

}
