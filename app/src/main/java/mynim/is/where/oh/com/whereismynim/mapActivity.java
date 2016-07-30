package mynim.is.where.oh.com.whereismynim;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;

public class mapActivity extends BaseActivity implements View.OnClickListener{

    private Button bt_close;
    private TextView geoInfo, updateInfo;
    private GoogleMap map;

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.bt_close:
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
                break;
            default: break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        bt_close = (Button)findViewById(R.id.bt_close);
        geoInfo = (TextView)findViewById(R.id.geoInfo);
        updateInfo = (TextView)findViewById(R.id.updateInfo);

        bt_close.setOnClickListener(this);

        Bundle intents = getIntent().getExtras();
        geoInfo.setText(intents.getString("geoInfo"));
        updateInfo.setText(intents.getString("updateInfo"));

        initMap();
    }

    public void initMap(){
        map.clear();

        map.addMarker(new MarkerOptions().position(MainActivity.now).title("현 위치"));
        PolylineOptions partPath = new PolylineOptions().color(getResources().getColor(R.color.colorAccent)).width(5.0f);
        for (int i = 0; i < MainActivity.partLoc.size(); i++) {
            partPath.add(MainActivity.partLoc.get(i).loc);
            map.addMarker(new MarkerOptions().position(MainActivity.partLoc.get(i).loc).title(MainActivity.partLoc.get(i).date).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
        map.addPolyline(partPath);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.nowYou, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
            overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
        }
        return super.onKeyDown(keyCode, event);
    }
}
