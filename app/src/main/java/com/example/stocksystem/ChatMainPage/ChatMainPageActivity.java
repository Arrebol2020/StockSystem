package com.example.stocksystem.ChatMainPage;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.stocksystem.OrderShow.BuyOrderUserActivity;
import com.example.stocksystem.OrderShow.SellOrderUserActivity;
import com.example.stocksystem.R;
import com.example.stocksystem.StockDeatailActivity;
import com.example.stocksystem.TestVoiceActivity;
import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.ArrayList;
import java.util.List;

public class ChatMainPageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edt_msg;
    private Button btn_send, select_btn;
    private TextView voice_tv;
    private List<MsgEntity> list;//存放对话框信息实体的集合
    boolean flag=false;//记录是否在待跳转状态
    int index=0;//记录跳向哪个界面
    int times = 0;
    private Resources resources;
    private String[] operations = new String[]{"购买", "卖出", "折线图", "查询股票", "持股", "历史订单"};
    private MsgEntity msg1=new MsgEntity(MsgEntity.RCV_MSG,"欢迎您本软件，小股为您服务！"+
            "\n"+"您可以通过发送指令或语音输入\n跳转到相关界面进行操作"+
            "\n"+"1. 购买股票 + 股票id(股票名称)"+
            "\n"+"2. 卖出股票 + 股票id(股票名称)"+
            "\n"+"3. 查询折线图 + 股票id(股票名称)"+
            "\n"+"4. 查询股票 + 股票id(股票名称)"+
            "\n"+"5. 查询持股信息"+
            "\n"+"6. 查询历史订单");
    static MsgAdapter msgAdapter;

    private static final String TAG = "ChatMainPageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_mainpage);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5eccbf6b");

        initView();//初始化控件
        initMsg();//初始化界面
        initListener();
    }


    private void initListener(){
        msgAdapter=new MsgAdapter(this,list);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(msgAdapter);

        select_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                Drawable drawable;
                if(times % 2 == 0){
                    drawable = resources.getDrawable(R.drawable.ic_keyboard_black_24dp);
                    voice_tv.setVisibility(View.GONE);
                    edt_msg.setVisibility(View.VISIBLE);
                }else{
                    drawable = resources.getDrawable(R.drawable.ic_keyboard_voice_black_24dp);
                    voice_tv.setVisibility(View.VISIBLE);
                    edt_msg.setVisibility(View.GONE);
                }
                select_btn.setBackground(drawable);
                times += 1;
            }
        });

        voice_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSpeech(ChatMainPageActivity.this);
            }
        });

        //点击发送后的监听事件
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edt_msg.getText().toString().trim();
                if(!TextUtils.isEmpty(content)){
                    MsgEntity send_msg=new MsgEntity(MsgEntity.SEND_MSG,content);
                    list.add(send_msg);
                    //刷新RecyclerView显示
                    msgAdapter.notifyItemInserted(list.size()-1);

                    //模拟接受消息
                    MsgEntity rcv_msg=null;
                    int code = getActivityCode(content);
                    Log.d(TAG, "onClick: " + code);
                    if(code != -1){
                        showDialog(content, code);
                    }else{
                        rcv_msg=new MsgEntity(MsgEntity.RCV_MSG,"小股不明白您的需求呢");
                        reciveMsg(rcv_msg, msgAdapter);
                    }
                }
//                String send_content=edt_msg.getText().toString().trim();
//                if (!TextUtils.isEmpty(send_content)){
//                    //模拟发送消息
//                    MsgEntity send_msg=new MsgEntity(MsgEntity.SEND_MSG,send_content);
//                    list.add(send_msg);
//                    //刷新RecyclerView显示
//                    msgAdapter.notifyItemInserted(list.size()-1);
//
//                    //模拟接受消息
//                    MsgEntity rcv_msg=null;
//                    if(send_content.equals("buy")||send_content.equals("sell")){
//                        rcv_msg=new MsgEntity(MsgEntity.RCV_MSG,"请确认是否跳转到"+send_content+"功能界面"+
//                                "\n"+"回复[yes]进行跳转");
//                        switch (send_content){//记录用户选择要跳转到哪个功能界面
//                            case "buy":
//                                index=1;
//                            case "sell":
//                                index=2;
//                            case "detail":
//                                index=3;
//                            case "orderlist":
//                                index=4;
//                        }
//                        flag=true;//此时为待跳转状态
//                        reciveMsg(rcv_msg,msgAdapter);
//                    }else {
//                        if(flag) {//如果此时是待跳转状态
//                            flag = false;
//                            if (send_content.equals("yes")) {//同意跳转，跳转界面
//                                switch (index) {
//                                    case 1:
////                                        startActivity(new Intent(ChatMainPageActivity.this, buyActivity.class));
//                                    case 2:
////                                        startActivity(new Intent(ChatMainPageActivity.this, sellActivity.class));
//                                    case 3:
////                                        startActivity(new Intent(ChatMainPageActivity.this, detailActivity.class));
//                                    case 4:
////                                        startActivity(new Intent(ChatMainPageActivityy.this, orderlistActivity.class));
//                                }
//                            }else {
//                                rcv_msg = new MsgEntity(MsgEntity.RCV_MSG, "您选择了不跳转"+"\n"+"请重新输入想要进行的操作");
//                                reciveMsg(rcv_msg,msgAdapter);
//                            }
//                        }else {
//                            rcv_msg = new MsgEntity(MsgEntity.RCV_MSG, "您的输入无效"+"\n"+"请重新输入想要进行的操作");
//                            reciveMsg(rcv_msg,msgAdapter);
//                        }
//                    }
//                    edt_msg.setText("");//清空消息输入框
//                }
                edt_msg.setText("");
            }
        });
    }

    private void reciveMsg( MsgEntity rcv_msg,MsgAdapter msgAdapter){
        list.add(rcv_msg);
        msgAdapter.notifyItemInserted(list.size()-1);
        //将RecyclerView将显示的数据定位到最后一行
        recyclerView.scrollToPosition(list.size()-1);
    }

    //初始化界面
    private void initMsg() {
        list=new ArrayList<MsgEntity>();
        list.add(msg1);
    }

    //初始化控件
    private void initView() {
        recyclerView=(RecyclerView)findViewById(R.id.recylerView);
        edt_msg= findViewById(R.id.edt_msg);
        btn_send= findViewById(R.id.btn_send);
        select_btn = findViewById(R.id.select_btn);
        voice_tv = findViewById(R.id.voice_tv);
        resources = ChatMainPageActivity.this.getResources();
    }


    /**
     * 初始化语音识别
     */
    public void initSpeech(final Context context){
        //创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        //设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if(!isLast){
                    //解析语音
                    String result = parseVoice(recognizerResult.getResultString());
                    edt_msg.setText(result);
                    edt_msg.setVisibility(View.VISIBLE);
                    voice_tv.setVisibility(View.GONE);
                    select_btn.setBackground(getDrawable(R.drawable.ic_keyboard_black_24dp));
                    times = 0;
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        //显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 解析语音json
     * @param resultString
     * @return
     */
    public String parseVoice(String resultString){
        Gson gson = new Gson();
        TestVoiceActivity.Voice voiceBean = gson.fromJson(resultString, TestVoiceActivity.Voice.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<TestVoiceActivity.Voice.WSBean> ws = voiceBean.ws;
        for (TestVoiceActivity.Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    /**
     * 语音对象封装
     */
    public class Voice {

        public ArrayList<TestVoiceActivity.Voice.WSBean> ws;

        public class WSBean {
            public ArrayList<TestVoiceActivity.Voice.CWBean> cw;
        }

        public class CWBean {
            public String w;
        }
    }

    private void showDialog(String content, final int code){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChatMainPageActivity.this);
        builder.setIcon(R.drawable.ic_question_answer_black_24dp);
        builder.setMessage("确定进入" + content + "吗");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (code){
                    case 0:
                        Intent intent0 = new Intent(ChatMainPageActivity.this, BuyOrderUserActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1 = new Intent(ChatMainPageActivity.this, SellOrderUserActivity.class);
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent(ChatMainPageActivity.this, StockDeatailActivity.class);
                        startActivity(intent2);
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    default:
                        break;

                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MsgEntity rcv_msg=new MsgEntity(MsgEntity.RCV_MSG,"您已取消跳转。");
                reciveMsg(rcv_msg, msgAdapter);
                reciveMsg(msg1, msgAdapter);
            }
        });
        builder.show();
    }

    private int getActivityCode(String content){
        for (int i = 0; i < operations.length; i++) {
            if(content.contains(operations[i])){
                return i;
            }
        }
        return -1;
    }
}