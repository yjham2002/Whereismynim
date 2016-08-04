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
    private String dbName = "Whereismynim";
    private String createTable = "create table chatList (byme integer, idx integer, isSent integer, msg text, date text, caller integer, myname integer);";

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

    private void insertData(boolean byme, int idx, int isSent, String msg, String date, int caller, int myname){
        String sqla = "select MAX(idx) from chatList";
        int idxnum = (int)database.compileStatement("select max(idx) FROM chatList").simpleQueryForLong();
        database.beginTransaction();
        int by = byme ? 1 : 0;
        try{
            String sql = "insert into chatList values ("+ by +", " + (++idxnum) + ", "+ isSent + ", '" + msg + "', '"+ date +"' ,'" + caller + "', '"+ myname +"');";
            database.execSQL(sql);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
    }

    public void selectData(){
        String sql = "select * from chatList where caller = " + MainActivity.part_key + " and myname = "+ MainActivity.user_key +" order by idx asc";
        Cursor result = database.rawQuery(sql, null);
        result.moveToFirst();
        cAdapter.mListData.clear();
        while(!result.isAfterLast()){
            cAdapter.addItem(result.getString(0).charAt(0)-'0'==1, Integer.parseInt(result.getString(1)), result.getString(2).charAt(0)-'0',result.getString(3), result.getString(4), Integer.parseInt(result.getString(5)));
            result.moveToNext();
        }
        cAdapter.dataChange();
        result.close();
    }

    public void dropTable(){
        database.beginTransaction();
        try{
            String sql = "drop table chatList";
            database.execSQL(sql);
            database.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            database.endTransaction();
        }
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
        nMgr.cancel(2014112021);

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
                int position = cAdapter.mListData.get(arg2).idx;
                final String sql = "delete from chatList where idx = " + position;
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
                if(msg.getText().length()<=0) break;
                else {
                    Calendar cal = Calendar.getInstance();
                    String dateSet = Integer.toString(cal.get(Calendar.YEAR))+"-"+Integer.toString(cal.get(Calendar.MONTH)+1)+"-"+
                            Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+" "+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+":"+Integer.toString(cal.get(Calendar.MINUTE))+":"+Integer.toString(cal.get(Calendar.SECOND));
                    insertData(true, 0, 0, msg.getText().toString(), dateSet, MainActivity.part_key, MainActivity.user_key);
                    selectData();
                    HashMap<String, String> data = new HashMap<>();
                    data.put("id", Integer.toString(MainActivity.part_key));
                    data.put("title", "MSG_CALL");
                    data.put("message", msg.getText().toString());
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
