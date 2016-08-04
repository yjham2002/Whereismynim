package mynim.is.where.oh.com.whereismynim;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;

    private ImageView bt_close;
    private Button set_modi, set_exit, set_cycle;
    private Switch set_auto, set_acc, set_noti;
    private LinearLayout scroller;
    private ParallaxScrollView pscroll;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_modi:
                showToast("준비중입니다.");
                break;
            case R.id.set_noti:
                prefEditor.putBoolean("notiacc", set_noti.isChecked());
                prefEditor.commit();
                break;
            case R.id.set_acc:
                prefEditor.putBoolean("force", set_acc.isChecked());
                prefEditor.commit();
                break;
            case R.id.set_auto:
                prefEditor.putBoolean("alarm", set_auto.isChecked());
                prefEditor.commit();
                break;
            case R.id.set_cycle:
                prefEditor.putInt("ctime", 3);
                break;
            case R.id.set_exit:
                showToast("준비중입니다.");
                break;
            case R.id.bt_exit:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        prefEditor = prefs.edit();

        scroller = (LinearLayout)findViewById(R.id.scroller);
        pscroll = (ParallaxScrollView)findViewById(R.id.pscroll);

        set_modi = (Button)findViewById(R.id.set_modi);
        set_exit = (Button)findViewById(R.id.set_exit);
        set_cycle = (Button)findViewById(R.id.set_cycle);

        set_auto = (Switch)findViewById(R.id.set_auto);
        set_acc = (Switch)findViewById(R.id.set_acc);
        set_noti = (Switch)findViewById(R.id.set_noti);

        set_auto.setChecked(prefs.getBoolean("alarm", true));
        set_acc.setChecked(prefs.getBoolean("force", true));
        set_noti.setChecked(prefs.getBoolean("notiacc", true));

        bt_close = (ImageView)findViewById(R.id.bt_exit);
        bt_close.setOnClickListener(this);

        set_modi.setOnClickListener(this);
        set_exit.setOnClickListener(this);
        set_cycle.setOnClickListener(this);
        set_auto.setOnClickListener(this);
        set_acc.setOnClickListener(this);
        set_noti.setOnClickListener(this);

        new SizeS().execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
            if(pscroll.getHeight() > scroller.getHeight()) scroller.getLayoutParams().height = pscroll.getHeight();
            //parallex.getLayoutParams().height = parentView.getHeight();
            pscroll.setTopBound(pscroll.getHeight());
            super.onPostExecute(result);
        }
    }

}
