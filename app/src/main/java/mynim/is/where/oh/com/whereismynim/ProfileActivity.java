package mynim.is.where.oh.com.whereismynim;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    private ImageView bt_close;
    private TextView name, email, pname, pdate, pr_auto, pr_noti, pr_acc;

    private LinearLayout scroller;
    private ParallaxScrollView pscroll;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
        setContentView(R.layout.activity_profile);

        final SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = prefs.edit();

        scroller = (LinearLayout)findViewById(R.id.scroller);
        pscroll = (ParallaxScrollView)findViewById(R.id.pscroll);

        name = (TextView)findViewById(R.id.Me);
        email = (TextView)findViewById(R.id.Mail);

        pname = (TextView)findViewById(R.id.pro_pname);
        pdate = (TextView)findViewById(R.id.condate);
        pr_acc = (TextView)findViewById(R.id.pr_acc);
        pr_auto = (TextView)findViewById(R.id.pr_auto);
        pr_noti = (TextView)findViewById(R.id.pr_noti);

        name.setText(MainActivity.user_name);
        email.setText(MainActivity.user_id);

        pname.setText("상대방 이름 : " + MainActivity.part_name);
        pdate.setText(MainActivity.currentInfo);

        pr_acc.setText(prefs.getBoolean("alarm", true) ? "위치 자동 기록 동의 : 허용" : "위치 자동 기록 동의 : 거부");
        pr_auto.setText(prefs.getBoolean("alarm", true) ? "위치 자동 기록 주기 : " + prefs.getInt("ctime", 3) + " 시간" : "허용되지 않음");
        pr_noti.setText(prefs.getBoolean("notiacc", true) ? "알림 표시 여부 : 허용" : "알림 표시 여부 : 거부");

        bt_close = (ImageView)findViewById(R.id.bt_exit);
        bt_close.setOnClickListener(this);

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
