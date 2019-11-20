package com.zyl.custompwdinputview;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class SecurityCodeActivity extends Activity implements View.OnClickListener {
    public static final String TAG = SecurityCodeActivity.class.getSimpleName();
    Button send;
    EditText inputcodePhone;
    TextView inputcodeTime;
    PinEntryEditText inputcodeEditlinear;
    Button inputcodeNext;
    //验证码重新发送剩余时间
    private long laveTime;
    //验证码
    private String code;

    private String phone_number;
    EventHandler eventHandler;
    private boolean coreflag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputcode);
        inputcodePhone = findViewById(R.id.inputcode_phone);
        inputcodePhone.setOnClickListener(this);
        inputcodeTime = findViewById(R.id.inputcode_time);
        inputcodeTime.setOnClickListener(this);
        inputcodeEditlinear = findViewById(R.id.inputcode_editlinear);
        inputcodeEditlinear.setOnClickListener(this);
        inputcodeNext = findViewById(R.id.inputcode_next);
        inputcodeNext.setOnClickListener(this);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);

        //设置输入框监听
        inputcodeEditlinear.addTextChangedListener(new EditChangedListener());
        //倒计时
        laveTime = 60000;
        sms_verification();
    }

    CountDownTimerUtils mCountDownTimerUtils;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //返回
            case R.id.inputcode_back:
                finish();
                break;
            //下一步
            case R.id.inputcode_next:
                if(judCord()&&judPhone()) {//判断验证码
                    SMSSDK.submitVerificationCode("86", phone_number, code);//提交手机号和验证码
                }
                coreflag=false;
                break;
            //获取验证码
            case R.id.send:
                if(judPhone()){
                    SMSSDK.getVerificationCode("86",phone_number);//获取你的手机号的验证码
                    inputcodeEditlinear.requestFocus();//判断是否获得焦点
                    //重新开始倒计时
                    mCountDownTimerUtils = new CountDownTimerUtils(inputcodeTime, laveTime, 1000);
                    mCountDownTimerUtils.start();
                }

                break;
        }
    }

    /**
     * 设置EditText监听
     */
    class EditChangedListener implements TextWatcher {
        private final int charMaxNum = 4;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            code = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().length() == charMaxNum) {
                inputcodeNext.setEnabled(true);
                inputcodeNext.setBackgroundResource(R.drawable.shape_button);
            } else {
                inputcodeNext.setBackgroundResource(R.drawable.shape_noclick);
                inputcodeNext.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == charMaxNum) {
                inputcodeNext.setEnabled(true);
                inputcodeNext.setBackgroundResource(R.drawable.shape_button);
                code = s.toString();
            } else {
                inputcodeNext.setBackgroundResource(R.drawable.shape_noclick);
                inputcodeNext.setEnabled(false);
            }
        }
    }

    /**
     * 时间倒计时
     */
    private TextView mTextView;
    class CountDownTimerUtils extends CountDownTimer {
        public CountDownTimerUtils(TextView textView, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mTextView = textView;
        }

        @Override
        public void onTick(long l) {
            send.setEnabled(false);
            mTextView.setClickable(false); //设置不可点击
            mTextView.setText(l / 1000 + "s");  //设置倒计时时间
            SpannableString spannableString = new SpannableString(mTextView.getText().toString());
            ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
            spannableString.setSpan(span, 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            mTextView.setText(spannableString);

        }

        @Override
        public void onFinish() {
            mTextView.setClickable(true);//重新获得点击
            mTextView.setText("重新发送");
            send.setEnabled(true);
        }
    }

    private boolean judPhone() {//判断手机号是否正确
        //不正确的情况
        if(TextUtils.isEmpty(inputcodePhone.getText().toString().trim()))//对于字符串处理Android为我们提供了一个简单实用的TextUtils类，如果处理比较简单的内容不用去思考正则表达式不妨试试这个在android.text.TextUtils的类，主要的功能如下:
        //是否为空字符 boolean android.text.TextUtils.isEmpty(CharSequence str)
        {
            Toast.makeText(this,"请输入您的电话号码",Toast.LENGTH_LONG).show();
            inputcodePhone.requestFocus();//设置是否获得焦点。若有requestFocus()被调用时，后者优先处理。注意在表单中想设置某一个如EditText获取焦点，光设置这个是不行的，需要将这个EditText前面的focusable都设置为false才行。
            inputcodeEditlinear.setText("");
            return false;
        }
        else if(inputcodePhone.getText().toString().trim().length()!=11){
            Toast.makeText(this,"您的电话号码位数不正确",Toast.LENGTH_LONG).show();
            inputcodePhone.requestFocus();
            inputcodeEditlinear.setText("");
            return false;
        }

        //正确的情况
        else{
            phone_number=inputcodePhone.getText().toString().trim();
            String num="[1][3578]\\d{9}";
            if(phone_number.matches(num)) {
                return true;
            }
            else{
                Toast.makeText(this,"请输入正确的手机号码",Toast.LENGTH_LONG).show();
                inputcodeEditlinear.setText("");
                return false;
            }
        }
    }


    private boolean judCord() {//判断验证码是否正确
        judPhone();//先执行验证手机号码正确与否
        if(TextUtils.isEmpty(inputcodeEditlinear.getText().toString().trim())) {//验证码
            Toast.makeText(this, "请输入您的验证码", Toast.LENGTH_LONG).show();
           inputcodeEditlinear.requestFocus();//聚集焦点
            return false;
        }
        else if(inputcodeEditlinear.getText().toString().trim().length()!=4){
            Toast.makeText(this,"您的验证码位数不正确",Toast.LENGTH_LONG).show();
            inputcodeEditlinear.requestFocus();
            inputcodeEditlinear.setText("");
            return false;
        }
        else{
            code=inputcodeEditlinear.getText().toString().trim();
            return true;
        }
    }

    public void sms_verification(){
        eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg=new Message();//创建了一个对象
                msg.arg1=event;
                msg.arg2=result;
                msg.obj=data;
                handler.sendMessage(msg);
            }
        };

        SMSSDK.registerEventHandler(eventHandler);//注册短信回调（记得销毁，避免泄露内存）*/
    }

    /**
     * 使用Handler来分发Message对象到主线程中，处理事件
     */
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event=msg.arg1;
            int result=msg.arg2;
            Object data=msg.obj;
            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//获取验证码成功
                if(result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    boolean smart = (Boolean)data;
                    if(smart) {
                        Toast.makeText(getApplicationContext(),"该手机号已经注册过，请重新输入",Toast.LENGTH_LONG).show();
                        inputcodePhone.requestFocus();//焦点
                        return;
                    }
                }
            }
            //回调完成
            if (result==SMSSDK.RESULT_COMPLETE){
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "验证码输入正确",Toast.LENGTH_LONG).show();
                    //启动隐藏图标的APK
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.szzt.advert", "com.szzt.advert.MainActivity"));
                    startActivity(intent);
                }
            }else {//其他出错情况
                if(coreflag){
                    Toast.makeText(getApplicationContext(),"验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
                    inputcodePhone.requestFocus();
                    inputcodeEditlinear.setText("");
                }
                else{
                    Toast.makeText(getApplicationContext(),"验证码输入错误", Toast.LENGTH_LONG).show();
                    inputcodeEditlinear.setText("");
                }

            }
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
        mCountDownTimerUtils.cancel();
    }

}
