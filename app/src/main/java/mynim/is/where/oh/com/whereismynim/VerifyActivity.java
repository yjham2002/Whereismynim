package mynim.is.where.oh.com.whereismynim;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

public class VerifyActivity extends BaseActivity implements View.OnClickListener {

    public static final int MY_PERMISSION_ACCESS_SMS = 2014112022;

    private Button request;
    private EditText userAcc;

    public Intent reqIn;

    private boolean requested = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefEditor;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.request:
                if(requested){

                    final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("취소 요청을 보내는중...");
                    progressDialog.show();

                    HashMap<String, String> mapD = new HashMap<>();
                    mapD.put("id", Integer.toString(MainActivity.user_key));
                    new Communicator().postHttp(AddInfo.URL_DEL_COUPLE, mapD, new Handler(){
                        @Override
                        public void handleMessage(Message msg){
                            String jsonString = msg.getData().getString("jsonString").trim();
                            if(jsonString.equals("END")) {
                                showToast("취소가 완료되었습니다. 다시 로그인해주시기 바랍니다.");
                                progressDialog.dismiss();
                                startActivity(reqIn);
                                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                                finish();
                            }else{
                                showToast("요청을 보내는 중 오류가 발생하였습니다.");
                            }
                        }
                    });
                }else {
                    if(userAcc.length() <= 0){
                        showToast("상대방의 이메일을 입력하세요.");
                        break;
                    }else if(userAcc.getText().toString().equals(MainActivity.user_id)){
                        showToast("본인의 아이디는 입력할 수 없습니다.");
                        break;
                    }

                    if ( Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity)this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSION_ACCESS_SMS);
                    }
                    else{
                        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("연결 요청을 보내는중...");
                        progressDialog.show();

                        Communicator userAuth = new Communicator();
                        userAuth.getHttp(AddInfo.URL_USER_COUNT + userAcc.getText().toString(), new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                String jsonString = msg.getData().getString("jsonString");
                                try{
                                    if(Integer.parseInt(jsonString.toString()) == 1){
                                        HashMap<String, String> mapData = new HashMap();
                                        mapData.put("id1", MainActivity.user_id);
                                        mapData.put("id2", userAcc.getText().toString());
                                        new Communicator().postHttp(AddInfo.URL_REG_COUPLE, mapData, new Handler(){
                                            @Override
                                            public void handleMessage(Message msg) {
                                                String jsonString = msg.getData().getString("jsonString").trim();
                                                if(jsonString.equals("ACK")){
                                                    showToast("요청이 완료되었습니다. 다시 로그인해주시기 바랍니다.");
                                                    progressDialog.dismiss();
                                                    startActivity(reqIn);
                                                    overridePendingTransition(R.anim.push_in, R.anim.push_out);
                                                    finish();
                                                }else if(jsonString.equals("DEN")){
                                                    showToast("요청할 수 없는 사용자입니다.");
                                                    progressDialog.dismiss();
                                                }else{
                                                    showToast("오류가 발생하였습니다.");
                                                    progressDialog.dismiss();
                                                }
                                            }
                                            });
                                    }else{
                                        showToast("요청할 수 없는 사용자입니다.");
                                        progressDialog.dismiss();
                                    }
                                }catch(Exception e){
                                    showToast("오류가 발생하였습니다.");
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VerifyActivity.MY_PERMISSION_ACCESS_SMS: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("동의가 완료되었습니다.\n상대방에게 다시 요청하세요.");
                } else {
                    showToast("권한에 동의하지 않아 진행할 수 없습니다.");
                }
                return;
            }
            default: break;
        }
    }

    public void validate(){
        prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
        prefEditor = prefs.edit();

        prefEditor.putInt("user_key", MainActivity.user_key);
        prefEditor.putInt("part_key", MainActivity.part_key);
        prefEditor.commit();

        switch(getIntent().getExtras().getInt("code")){
            case 0:
                requested = false;
                break;
            case 1:
                requested = true;
                break;
            case 2:
                Intent i = new Intent(this, RequestActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                finish();
                break;
            case 3:
                Intent i2 = new Intent(this, MainActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                finish();
                break;
            default: break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        validate();

        setContentView(requested ? R.layout.activity_verify2 : R.layout.activity_verify);

        reqIn = new Intent(this, LoginActivity.class);

        if(requested){
            request = (Button)findViewById(R.id.request);
            request.setOnClickListener(this);
        }else{
            userAcc = (EditText)findViewById(R.id.idp);
            request = (Button)findViewById(R.id.request);
            request.setOnClickListener(this);
        }
    }

    public void sendSMS(String smsNumber, String smsText){

        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "요청 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        showToast("요청 실패");
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
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

}
