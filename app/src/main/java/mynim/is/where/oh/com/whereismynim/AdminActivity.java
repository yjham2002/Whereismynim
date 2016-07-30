package mynim.is.where.oh.com.whereismynim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class AdminActivity extends BaseActivity implements View.OnClickListener {

    private EditText id, title, msg;
    private Button send;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.admin_send:
                HashMap<String, String> data = new HashMap<>();
                data.put("id", id.getText().toString());
                data.put("title", title.getText().toString());
                data.put("message", msg.getText().toString());
                new Communicator().postHttp(AddInfo.URL_SEND, data, new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        showToast(msg.getData().getString("jsonString").trim());
                    }
                });
                break;
            default: break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        id = (EditText)findViewById(R.id.admin_id);
        title = (EditText)findViewById(R.id.admin_title);
        msg = (EditText)findViewById(R.id.admin_message);

        send = (Button)findViewById(R.id.admin_send);

        send.setOnClickListener(this);
    }
}
