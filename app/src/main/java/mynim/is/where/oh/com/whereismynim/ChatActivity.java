package mynim.is.where.oh.com.whereismynim;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

public class ChatActivity extends BaseActivity implements View.OnClickListener{

    private EditText msg;
    private TextView title;
    private ListView chat;
    private Button exit, send;
    private ChatAdapter cAdapter;

    public void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private SQLiteDatabase database;
    private String dbName = "WMN_DB";
    private String createTable =
            "create table if not exists WMN_CHAT(" +
            "`id` integer primary key autoincrement, " +
            "`from` integer, " +
            "`to` integer, " +
            "`msg` text, " +
            "`date` datetime, " +
            "`read` integer);";

    public void createDatabase(){
        database = openOrCreateDatabase(dbName, android.content.Context.MODE_PRIVATE, null);
    }

    public void createTable(){
        try{
            database.execSQL(createTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertData(int from, int to, String msg){
        database.beginTransaction();
        try{
            String sql = "insert into WMN_CHAT(`from`, `to`, `msg`, `date`, read) values (" + from + ", " + to + ", '" + msg + "', datetime('now', 'localtime') ," + 0 + ");";
            database.execSQL(sql);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }

    public void selectData(){
        String sql = "select * from WMN_CHAT where `from` = " + MainActivity.user_key + " OR `to` = "+ MainActivity.user_key +" order by `id` asc";
        Cursor result = database.rawQuery(sql, null);
        result.moveToFirst();
        cAdapter.mListData.clear();
        while(!result.isAfterLast()){
            cAdapter.addItem(result.getInt(0), result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getInt(5));
            result.moveToNext();
        }
        cAdapter.dataChange();
        result.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        View root = findViewById(R.id.rootLay);
        root.requestFocus();

        createDatabase();
        createTable();

        MainActivity.isRun = true;
        NotificationManager nMgr = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(MyGcmListenerService.MSG_CHT);
        nMgr.cancel(MyGcmListenerService.MSG_DEF);

        msg = (EditText)findViewById(R.id.msg);
        title = (TextView)findViewById(R.id.title);
        chat = (ListView)findViewById(R.id.chat);
        //exit = (Button)findViewById(R.id.exit);
        send = (Button)findViewById(R.id.send);

        //exit.setOnClickListener(this);
        send.setOnClickListener(this);

        cAdapter = new ChatAdapter(this);

        chat.setAdapter(cAdapter);

        selectData();

        title.setText(MainActivity.part_name);

        chat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int position = cAdapter.mListData.get(arg2).id;
                final String sql = "delete from WMN_CHAT where `id` = " + position;
                showToast("메세지 삭제됨");
                database.execSQL(sql);
                selectData();
                return false;
            }
        });

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //case R.id.exit:
              //  finish();
                //break;
            case R.id.send:
                if(msg.getText().length() > 0) {
                    insertData(MainActivity.user_key, MainActivity.part_key, msg.getText().toString());
                    selectData();
                    HashMap<String, String> data = new HashMap<>();
                    data.put("id", Integer.toString(MainActivity.part_key));
                    data.put("title", "MSG_CALL");
                    data.put("message", msg.getText().toString());
                    data.put("froma", Integer.toString(MainActivity.user_key));
                    data.put("toa", Integer.toString(MainActivity.part_key));
                    new Communicator().postHttp(AddInfo.URL_SEND, data, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            //if(msg.getData().getString("jsonString").trim().length() <=0) showToast("전송 실패");
                        }
                    });
                    msg.setText("");
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("WMN_CHAT_EVENT"));
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            selectData();
        }
    };
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MainActivity.isRun = false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            finish();
            overridePendingTransition(R.anim.push_in_r, R.anim.push_out_r);
        }
        return super.onKeyDown(keyCode, event);
    }

}
