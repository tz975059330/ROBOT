package com.king.myrobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.speech.VoiceRecognitionService;
import com.turing.androidsdk.InitListener;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

public class MainActivity extends Activity implements OnClickListener,RecognitionListener {

    //TuringAPI管理类
    private TuringApiManager mTuringApiManager;
    //申请机器人的key
    private final String TURING_AIPKEY = "7bafdd39a1ca4b93ad4a657fcb3b5712";
    //申请的secret
    private final String TURING_SECRET = "3ef190e2711195fc";
    //给用户分配的唯一标示
    private final String TURING_USERID = "KingNoQueen";
    //
    private SDKInitBuilder builder;
    public static final int STATUS_None = 0;
    private SpeechRecognizer speechRecognizer;
    private int status = STATUS_None;
    private Button mBtnSend;
    private Button mBtnBack;
    private Button mBtnSpeak;
    private EditText mEditTextContent;
    //聊天内容的适配器
    private ChatMsgViewAdpater mAdapter;
    private ListView mListView;
    //聊天的内容
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();

    private boolean isPushOpen  = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Resources resource = this.getResources();
        String pkgName = this.getPackageName();
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(R.layout.chat);
        initView();
        initData();
        initRobot();
        //启动推送服务
        PushManager.startWork(MainActivity.this,PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(MainActivity.this,"api_key"));
        //Log.d(MainActivity.class.getSimpleName(),"绑定执行");

    }

    /**
     * t图灵机器人初始化相关的内容
     */
    private void initRobot() {
        //初始化turingSDK
        builder= new SDKInitBuilder(MainActivity.this);
        builder.setSecret(TURING_SECRET);
        builder.setTuringKey(TURING_AIPKEY);
        builder.setUniqueId(TURING_USERID);
        //图灵机器人初始化方法
        SDKInit.init(builder, new InitListener() {
            @Override
            public void onComplete() {
                // TODO Auto-generated method stub
                //获取userid成功后请求turing服务器，需要请求必须在这块回调成功才能正确请求
                mTuringApiManager = new TuringApiManager(MainActivity.this);
                //设置网络连接
                mTuringApiManager.setHttpListener(myHttpConnectionListener);
            }

            @Override
            public void onFail(String s) {

            }
        });
    }

    //初始化视图
    private void initView() {
        mListView = (ListView) findViewById(R.id.listview);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnSpeak = (Button) findViewById(R.id.speak);
        mBtnSend.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);
        mBtnSpeak.setOnClickListener(this);
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

    }


    private void stop() {
        speechRecognizer.stopListening();
    }

    private void cancel() {
        speechRecognizer.cancel();
        status = STATUS_None;
    }
    // 开始识别
    private void start() {
        Toast.makeText(MainActivity.this,"开始说话",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        bindParams(intent);
        speechRecognizer.startListening(intent);

    }
    /**
     * 设置识别参数
     * @param intent
     */
    public void bindParams(Intent intent) {
        intent.putExtra("sample", 16000); // 离线仅支持16000采样率
        intent.putExtra("language", "cmn-Hans-CN"); // 离线仅支持中文普通话
        intent.putExtra("prop", 20000); // 输入

    }
    /**
     * 网络请求回调
     */
    HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {
        @Override
        public void onError(ErrorMessage errorMessage) {

        }
        @Override
        public void onSuccess(RequestResult requestResult) {
            if(null!=requestResult){
                try {

                    JSONObject result_obj = new JSONObject(requestResult.getContent().toString());
                    if(result_obj.has("text")){
                        String contString = result_obj.get("text").toString();
                        if (contString.length() > 0)
                        {
                            ChatMsgEntity entity = new ChatMsgEntity();
                            entity.setDate(getDate());
                            entity.setName("Soul");
                            entity.setComMeg(true);
                            entity.setText(contString);
                            mDataArrays.add(entity);
                            mAdapter.notifyDataSetChanged();
                            mEditTextContent.setText("");
                            mListView.setSelection(mListView.getCount() - 1);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    //初始化要显示的数据
    private void initData() {

        mAdapter = new ChatMsgViewAdpater(this, mDataArrays);
        mListView.setAdapter(mAdapter);
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_send:
                send();
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.speak:
                start();
                break;
        }
    }

    private void send()
    {
        String contString = mEditTextContent.getText().toString();
        if (contString.length() > 0)
        {
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setDate(getDate());
            entity.setName("主人");
            entity.setComMeg(false);
            entity.setText(contString);
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            mTuringApiManager.requestTuringAPI(contString);
        }

    }

    //获取日期
    private String getDate() {
        Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String mins = String.valueOf(c.get(Calendar.MINUTE));
        StringBuffer sbBuffer = new StringBuffer();
        sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);
                      return sbBuffer.toString();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return true;

    }

    @Override
    public void onBeginningOfSpeech() {
        // 开始说话处理
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        // 准备就绪
    }

    @Override
    public void onRmsChanged(float v) {
        // 音量变化处理
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        // 录音数据传出处理
    }

    @Override
    public void onEndOfSpeech() {
        // 说话结束处理
    }

    @Override
    public void onError(int i) {
        // 出错处理
        switch(i){
            case SpeechRecognizer.ERROR_AUDIO:
                Toast.makeText(MainActivity.this,"音频问题",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                Toast.makeText(MainActivity.this,"没有语音输入",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                Toast.makeText(MainActivity.this,"其它客户端错误",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                Toast.makeText(MainActivity.this,"权限不足",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                Toast.makeText(MainActivity.this,"网络问题",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                Toast.makeText(MainActivity.this,"没有匹配的识别结果",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Toast.makeText(MainActivity.this,"引擎忙",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                Toast.makeText(MainActivity.this,"服务端错误",Toast.LENGTH_SHORT);
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                Toast.makeText(MainActivity.this,"连接超时",Toast.LENGTH_SHORT);
                break;
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        status = STATUS_None;
        ArrayList<String> nbest = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String result = Arrays.toString(nbest.toArray(new String[nbest.size()]));
        String contString = result;
        if (contString.length() > 0)
        {
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setDate(getDate());
            entity.setName("主人");
            entity.setComMeg(false);
            entity.setText(contString);
            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();
            mEditTextContent.setText("");
            mListView.setSelection(mListView.getCount() - 1);
            mTuringApiManager.requestTuringAPI(contString);
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        // 临时结果处理
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        // 处理事件回调
    }
}

