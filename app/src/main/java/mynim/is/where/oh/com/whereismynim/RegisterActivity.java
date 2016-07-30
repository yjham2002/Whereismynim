package mynim.is.where.oh.com.whereismynim;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    public static final int ON_OK = 0, ON_RED = 1, ON_NET = 2, ON_FAIL = 3;

    public static boolean read = false;

    private EditText _nameText, _emailText, _mobileText, _passwordText, _passcText;
    private CheckBox acceptRule;
    private Button _signupButton, _linkButton, _show;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acceptRule:
                if(!read){
                    acceptRule.setChecked(false);
                    showToast("약관을 모두 읽고 동의해주세요.");
                }
                break;
            case R.id.bt_show:
                Intent i = new Intent(this, RuleActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_in, R.anim.push_out);
                break;
            case R.id.bt_link_login:
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
                break;
            case R.id.bt_signup:
                signup();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        read = false;

        _nameText = (EditText)findViewById(R.id.input_name);
        _emailText = (EditText)findViewById(R.id.input_email);
        _passcText = (EditText)findViewById(R.id.input_passwordc);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _mobileText = (EditText)findViewById(R.id.input_mobile);
        _mobileText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        _signupButton = (Button)findViewById(R.id.bt_signup);
        _linkButton = (Button)findViewById(R.id.bt_link_login);
        _show = (Button)findViewById(R.id.bt_show);
        _signupButton.setOnClickListener(this);
        _linkButton.setOnClickListener(this);
        _show.setOnClickListener(this);

        acceptRule = (CheckBox)findViewById(R.id.acceptRule);
        acceptRule.setChecked(false);
        acceptRule.setOnClickListener(this);
    }

    public void signup() {
        if (!validate()) {
            onSignupFinish(ON_FAIL);
            return;
        }
        _signupButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("회원가입 요청 진행중...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.
        Communicator reg = new Communicator();
        HashMap<String, String> postData = new HashMap<String, String>();
        postData.put("id", email);
        postData.put("pw", password);
        postData.put("name", name);
        postData.put("mobile", mobile);
        reg.postHttp(AddInfo.URL_ADD_ACCOUNT, postData, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String jsonString = msg.getData().getString("jsonString").trim();
                try {
                    if (jsonString.equals("SUCC")) {
                        onSignupFinish(ON_OK);
                    } else if (jsonString.equals("FAIL")) {
                        onSignupFinish(ON_RED);
                    } else {
                        onSignupFinish(ON_NET);
                    }
                } catch (Exception e){
                    onSignupFinish(ON_NET);
                }finally {
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void onSignupFinish(int mode) {
        _signupButton.setEnabled(true);
        switch(mode){
            case ON_OK:
                showToast("회원가입이 완료되었습니다.");
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
                break;
            case ON_RED:
                showToast("중복되는 아이디가 존재합니다.");
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

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String passwordc = _passcText.getText().toString();

        if (name.isEmpty() || name.length() < 2) {
            _nameText.setError("최소 두 글자 이상 입력하세요");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("정상적인 이메일 주소를 입력하세요");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.PHONE.matcher(mobile).matches()) {
            _mobileText.setError("정상적인 휴대전화 번호를 입력하세요");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 5) {
            _passwordText.setError("5자 이상으로 입력하세요");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (!password.equals(passwordc)) {
            _passcText.setError("패스워드가 일치하지 않습니다");
            valid = false;
        } else {
            _passcText.setError(null);
        }

        if(!acceptRule.isChecked()){
            showToast("약관에 동의해주세요.");
            valid = false;
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
            finish();
            overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
        }
        return super.onKeyDown(keyCode, event);
    }

}
