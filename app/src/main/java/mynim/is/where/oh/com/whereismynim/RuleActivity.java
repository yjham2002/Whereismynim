package mynim.is.where.oh.com.whereismynim;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class RuleActivity extends BaseActivity implements View.OnClickListener {

    private Button close;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                RegisterActivity.read = true;
                finish();
                overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);

        close = (Button)findViewById(R.id.close);
        close.setOnClickListener(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
            overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
        }
        return super.onKeyDown(keyCode, event);
    }
}
