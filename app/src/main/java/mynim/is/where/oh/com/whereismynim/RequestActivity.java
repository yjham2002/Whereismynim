package mynim.is.where.oh.com.whereismynim;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RequestActivity  extends BaseActivity implements View.OnClickListener {

    private Button requestAc, requestDe;
    private TextView state;
    public Intent reqIn;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.requestAc:
                final ProgressDialog progressDialog2 = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                progressDialog2.setIndeterminate(true);
                progressDialog2.setMessage("연결 요청을 보내는중...");
                progressDialog2.show();

                HashMap<String, String> mapD2 = new HashMap<>();
                mapD2.put("id", Integer.toString(MainActivity.user_key));
                new Communicator().postHttp(AddInfo.URL_ACC_COUPLE, mapD2, new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        String jsonString = msg.getData().getString("jsonString").trim();
                        if(jsonString.equals("END")) {
                            showToast("연결이 완료되었습니다. 다시 로그인해주시기 바랍니다.");
                            progressDialog2.dismiss();
                            startActivity(reqIn);
                            overridePendingTransition(R.anim.push_in, R.anim.push_out);
                            finish();
                        }else{
                            showToast("요청을 보내는 중 오류가 발생하였습니다.");
                        }
                    }
                });
                break;
            case R.id.requestDe:
                final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("거절 요청을 보내는중...");
                progressDialog.show();

                HashMap<String, String> mapD = new HashMap<>();
                mapD.put("id", Integer.toString(MainActivity.user_key));
                new Communicator().postHttp(AddInfo.URL_DEL_COUPLE, mapD, new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        String jsonString = msg.getData().getString("jsonString").trim();
                        if(jsonString.equals("END")) {
                            showToast("거절이 완료되었습니다. 다시 로그인해주시기 바랍니다.");
                            progressDialog.dismiss();
                            startActivity(reqIn);
                            overridePendingTransition(R.anim.push_in, R.anim.push_out);
                            finish();
                        }else{
                            showToast("요청을 보내는 중 오류가 발생하였습니다.");
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req);
        reqIn = new Intent(this, LoginActivity.class);

        state = (TextView)findViewById(R.id.state);

        requestAc = (Button)findViewById(R.id.requestAc);
        requestDe = (Button)findViewById(R.id.requestDe);
        requestAc.setOnClickListener(this);
        requestDe.setOnClickListener(this);

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("상대방 데이터를 불러오는중...");
        progressDialog.show();

        HashMap<String, String> mapData = new HashMap<>();
        mapData.put("id", Integer.toString(MainActivity.user_key));
        new Communicator().postHttp(AddInfo.URL_GET_USER, mapData, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String jsonString = msg.getData().getString("jsonString");
                try {
                    JSONObject json = new JSONObject(jsonString);
                    HashMap<String, String> mapData = new HashMap<>();
                    mapData.put("id", json.getString("partner"));
                    new Communicator().postHttp(AddInfo.URL_GET_USER, mapData, new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            String jsonString = msg.getData().getString("jsonString");
                            try {
                                JSONObject json = new JSONObject(jsonString);
                                state.setText(json.getString("name") + "(" + json.getString("id") + ")님으로부터의 요청");
                            }catch(JSONException e){
                            }
                        }
                    });
                } catch (JSONException o) {
                    showToast("상대방 데이터를 불러오는 중 오류가 발생하였습니다.");
                } finally {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
