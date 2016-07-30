package mynim.is.where.oh.com.whereismynim;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public static final int ON_OK = 0, ON_NET = 2, ON_FAIL = 3;

    public Intent verify;
    private EditText idPanel, pwPanel;
    private Button btIn, btUp;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.bt_signin:
                login();
                break;
            case R.id.bt_signup:
                Intent i2 = new Intent(this, RegisterActivity.class);
                startActivity(i2);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                break;
            default: break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verify = new Intent(this, VerifyActivity.class);
        idPanel = (EditText)findViewById(R.id.idp);
        pwPanel = (EditText)findViewById(R.id.pwp);

        btIn = (Button)findViewById(R.id.bt_signin);
        btUp = (Button)findViewById(R.id.bt_signup);

        btIn.setOnClickListener(this);
        btUp.setOnClickListener(this);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.i(TAG, "already sent token");
                } else {
                    Log.i(TAG, "error!");
                }
            }
        };

        if (checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void login(){

        if (!validate()) {
            onSigninFinish(ON_FAIL);
            return;
        }
        btIn.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("로그인하는 중...");
        progressDialog.show();

        final String email = idPanel.getText().toString();
        String password = pwPanel.getText().toString();

        Communicator logger = new Communicator();
        HashMap<String, String> postData = new HashMap<String, String>();
        postData.put("id", email);
        postData.put("pw", password);
        logger.postHttp(AddInfo.URL_LOG_ACCOUNT, postData, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final String jsonString = msg.getData().getString("jsonString").trim();
                try {
                    if (jsonString.equals("0")) {
                        progressDialog.dismiss();
                        onSigninFinish(ON_FAIL);
                    } else {
                        SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = prefs.edit();
                        prefEditor.putString("user_id", email);
                        prefEditor.commit();
                        MainActivity.user_key = Integer.parseInt(jsonString);
                        MainActivity.user_id = email;
                        Communicator.getHttp(AddInfo.URL_CLASSIFY + email, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                String classified = msg.getData().getString("jsonString").trim();
                                try{
                                    switch(classified){
                                        case "NOT":
                                            verify.putExtra("code", 0);
                                            break;
                                        case "REQ":
                                            verify.putExtra("code", 1);
                                            break;
                                        case "RED":
                                            verify.putExtra("code", 2);
                                            break;
                                        case "REG":
                                            verify.putExtra("code", 3);
                                            break;
                                        default:
                                            progressDialog.dismiss();
                                            onSigninFinish(ON_NET);
                                            break;
                                    }
                                    HashMap<String, String> mapdata = new HashMap<>();
                                    mapdata.put("token", MainActivity.user_token);
                                    mapdata.put("id", Integer.toString(MainActivity.user_key));
                                    new Communicator().postHttp(AddInfo.URL_REG_TOKEN, mapdata, new Handler() {
                                    });
                                    HashMap<String, String> mapData = new HashMap<>();
                                    mapData.put("id", Integer.toString(MainActivity.user_key));
                                    new Communicator().postHttp(AddInfo.URL_GET_USER, mapData, new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            String jsonString = msg.getData().getString("jsonString");
                                            try {
                                                JSONObject json = new JSONObject(jsonString);
                                                MainActivity.user_name = json.getString("name");

                                                SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
                                                SharedPreferences.Editor prefEditor = prefs.edit();
                                                prefEditor.putString("user_name", MainActivity.user_name);
                                                prefEditor.commit();
                                                MainActivity.part_key = Integer.parseInt(json.getString("partner"));
                                                HashMap<String, String> mapData = new HashMap<>();
                                                mapData.put("id", Integer.toString(MainActivity.part_key));
                                                new Communicator().postHttp(AddInfo.URL_GET_USER, mapData, new Handler() {
                                                    @Override
                                                    public void handleMessage(Message msg) {
                                                        String jsonString = msg.getData().getString("jsonString");
                                                        try {
                                                            JSONObject json = new JSONObject(jsonString);
                                                            MainActivity.part_name = json.getString("name");
                                                            SharedPreferences prefs = getSharedPreferences("WMN_PREF", MODE_PRIVATE);
                                                            SharedPreferences.Editor prefEditor = prefs.edit();
                                                            prefEditor.putString("part_name", MainActivity.part_name);
                                                            prefEditor.commit();
                                                            progressDialog.dismiss();
                                                            onSigninFinish(ON_OK);
                                                        } catch (Exception e) {
                                                            progressDialog.dismiss();
                                                            onSigninFinish(ON_NET);
                                                        }
                                                    }
                                                });

                                            } catch (Exception e) {
                                                progressDialog.dismiss();
                                                onSigninFinish(ON_NET);
                                            }
                                        }
                                    });
                                }catch(Exception e){
                                    progressDialog.dismiss();
                                    onSigninFinish(ON_NET);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    onSigninFinish(ON_NET);
                }
            }
        });
    }

    public void onSigninFinish(int mode) {
        btIn.setEnabled(true);
        switch(mode){
            case ON_OK:
                startActivity(verify);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                finish();
                break;
            case ON_NET:
                showToast("네트워크 오류가 발생하였습니다.");
                break;
            case ON_FAIL:
                showToast("올바른 정보를 입력하세요.");
                break;
            default: break;
        }
    }

    public boolean validate(){
        boolean valid = true;

        String email = idPanel.getText().toString();
        String password = pwPanel.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            idPanel.setError("정상적인 이메일 주소를 입력하세요");
            valid = false;
        } else {
            idPanel.setError(null);
        }

        if (password.isEmpty() || password.length() < 5) {
            pwPanel.setError("5자 이상으로 입력하세요");
            valid = false;
        } else {
            pwPanel.setError(null);
        }

        return valid;
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
