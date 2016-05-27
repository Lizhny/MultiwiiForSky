/*  MultiWii EZ-GUI
    Copyright (C) <2012>  Bartosz Szczygiel (eziosoft)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ezio.Sky.advanced;

/*舵量控制函数
 * 包含屏幕触控函数，将参数传递给手机屏幕绘图函数和飞控传递函数处理*/

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.lizhy.LizhySky.R;
import com.ezio.Sky.app.App;
import com.ezio.Sky.config.DeviceListActivity;
import com.ezio.Sky.mw.MultirotorData;
import com.ezio.Sky.radio.StickView;

public class ControlActivity extends Activity implements View.OnClickListener {

    private boolean killme = false;
    private Boolean isLocked = true;//是否锁定
    private Button mBtnConnect;
    private Button mBtnAccCalibration;
    private Button mBtnQENHight;
    private Button mBtnConfig;
    private Button mBtnHandsMode;
    private Button mBtnUnlock;
    private static final int REQUEST_CONNECT_DEVICE_MULTIWII = 1;
    boolean heightStick = false;
    private int i = 0;
    App app;
    Handler mHandler = new Handler();

    StickView s1;
    StickView s2;

    int[] CH8 = {1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500};

    private Runnable update = new Runnable() {
        @Override
        public void run() {

            app.mw.ProcessSerialData(app.loggingON);
            //
            // Log.d("aaa", "Throttle=" + String.valueOf(app.mw.rcThrottle));
            // Log.d("aaa", "Yaw=" + String.valueOf(app.mw.rcYaw));
            // ;
            // Log.d("aaa", "Pitch=" + String.valueOf(app.mw.rcPitch));
            // Log.d("aaa", "Roll=" + String.valueOf(app.mw.rcRoll));
            if (app.RadioMode == 4) {
                s1.SetPosition(app.mw.rcRoll, app.mw.rcPitch);
                s2.SetPosition(app.mw.rcYaw, app.mw.rcThrottle);
            }

            if (app.RadioMode == 3) {
                s1.SetPosition(app.mw.rcRoll, app.mw.rcThrottle);
                s2.SetPosition(app.mw.rcYaw, app.mw.rcPitch);
            }
            if (app.RadioMode == MultirotorData.LEFTHANDSMODE) {
                s1.SetPosition(app.mw.rcYaw, app.mw.rcThrottle);
                s2.SetPosition(app.mw.rcRoll, app.mw.rcPitch);
            }

            if (app.RadioMode == MultirotorData.RIGHTHANDSMODE) {
                s1.SetPosition(app.mw.rcYaw, app.mw.rcPitch);
                s2.SetPosition(app.mw.rcRoll, app.mw.rcThrottle);
            }

            // * 0rcRoll 1rcPitch 2rcYaw
            // 3rcThrottle 4rcAUX1 5rcAUX2
            // 6rcAUX3 7rcAUX4
            //

            // app.mw.SendRequestSetRawRC(CH8);
            app.Frequentjobs();

            // app.mw.SendRequest();
            app.mw.SendRequestMSP_SET_RAW_RC(CH8);

            app.frskyProtocol.ProcessSerialData(false);
            app.Frequentjobs();

            app.mw.SendRequest(app.MainRequestMethod);

            if (!killme)
                mHandler.postDelayed(update, app.RefreshRate);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();
        setContentView(R.layout.control_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        s1 = (StickView) findViewById(R.id.stickView1);
        s2 = (StickView) findViewById(R.id.stickView2);
        mBtnConnect = (Button) findViewById(R.id.bt_Connect);
        mBtnAccCalibration = (Button) findViewById(R.id.bt_AccCalibration);
        mBtnQENHight = (Button) findViewById(R.id.bt_QENHight);
        mBtnConfig = (Button) findViewById(R.id.bt_Config);
        mBtnHandsMode = (Button) findViewById(R.id.bt_HandsMode);
        mBtnUnlock = (Button) findViewById(R.id.bt_Unlock);
        mBtnConnect.setOnClickListener(this);
        mBtnAccCalibration.setOnClickListener(this);
        mBtnQENHight.setOnClickListener(this);
        mBtnConfig.setOnClickListener(this);
        mBtnHandsMode.setOnClickListener(this);
        mBtnUnlock.setOnClickListener(this);

        switch (app.RadioMode) {
            case MultirotorData.RIGHTHANDSMODE:
                mBtnHandsMode.setText(R.string.Right_hand);
                break;
            case MultirotorData.LEFTHANDSMODE:
                mBtnHandsMode.setText(R.string.Left_hand);
                break;
            case 3:
                mBtnHandsMode.setText(R.string.Unuse_hand_mode);
                break;
            case 4:
                mBtnHandsMode.setText(R.string.Unuse_hand_mode);
                break;
        }

        controlData();
    }

    private void controlData() {
        s1.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (app.RadioMode == MultirotorData.RIGHTHANDSMODE) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s1.SetPosition((s1.InputX(event.getX())),
                            // s1.InputY(event.getY())); // TODO
                            // REMOVE
                            CH8[1] = (int) s1.InputY(event.getY() * 7 / 10); // throttle
                            // pitch
                            CH8[2] = (int) s1.InputX(event.getX() * 7 / 10); // yaw
                            break;
                        case MotionEvent.ACTION_UP:
                            // s1.SetPosition(1500, s1.InputY(event.getY()));// TODO
                            // REMOVE
                            CH8[1] = 1500; // throttle
                            CH8[2] = 1500; // yaw
                            break;
                    }
                }
                if (app.RadioMode == MultirotorData.LEFTHANDSMODE) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s1.SetPosition((s1.InputX(event.getX())),
                            // s1.InputY(event.getY())); // TODO
                            // REMOVE
                            CH8[3] = (int) s1.InputY(event.getY() * 7 / 10); // throttle
                            CH8[2] = (int) s1.InputX(event.getX() * 7 / 10); // yaw
                            break;
                        case MotionEvent.ACTION_UP:
                            // s1.SetPosition(1500, s1.InputY(event.getY()));// TODO
                            // REMOVE
                            if (!heightStick) {

                            }
                            CH8[3] = 1050; // throttle
                            CH8[2] = 1500; // yaw
                            break;
                    }
                }

                if (app.RadioMode == 3) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s1.SetPosition((s1.InputX(event.getX())),
                            // s1.InputY(event.getY()));
                            CH8[3] = (int) s1.InputY(event.getY() * 7 / 10); // pitch
                            CH8[0] = (int) s1.InputX(event.getX() * 7 / 10);// roll
                            break;
                        case MotionEvent.ACTION_UP:
                            // s1.SetPosition(1500, 1500);
                            CH8[3] = 1500;
                            CH8[0] = 1500;
                            break;
                    }
                }
                if (app.RadioMode == 4) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s1.SetPosition((s1.InputX(event.getX())),
                            // s1.InputY(event.getY()));
                            CH8[1] = (int) s1.InputY(event.getY() * 7 / 10); // pitch
                            CH8[0] = (int) s1.InputX(event.getX() * 7 / 10);// roll
                            break;
                        case MotionEvent.ACTION_UP:
                            // s1.SetPosition(1500, 1500);
                            CH8[1] = 1500;
                            CH8[0] = 1500;
                            break;
                    }
                }
                return true;
            }
        });

        s2.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (app.RadioMode == 1) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s2.SetPosition((s2.InputX(event.getX())),
                            // s2.InputY(event.getY()));
                            CH8[3] = (int) s2.InputY(event.getY() * 7 / 10); // pitch
                            CH8[0] = (int) s2.InputX(event.getX() * 7 / 10);// roll
                            break;
                        case MotionEvent.ACTION_UP:
                            // s2.SetPosition(1500, 1500);
                            CH8[3] = 1050;
                            CH8[0] = 1500;
                            break;
                    }
                }
                if (app.RadioMode == 2) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s2.SetPosition((s2.InputX(event.getX())),
                            // s2.InputY(event.getY()));
                            CH8[1] = (int) s2.InputY(event.getY() * 7 / 10); // pitch
                            CH8[0] = (int) s2.InputX(event.getX() * 7 / 10);// roll
                            break;
                        case MotionEvent.ACTION_UP:
                            // s2.SetPosition(1500, 1500);
                            CH8[1] = 1500;
                            CH8[0] = 1500;
                            break;
                    }
                }
                if (app.RadioMode == 3) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s2.SetPosition((s2.InputX(event.getX())),
                            // s2.InputY(event.getY())); // TODO
                            // REMOVE
                            CH8[1] = (int) s2.InputY(event.getY() * 7 / 10); // throttle
                            // pitch
                            CH8[2] = (int) s2.InputX(event.getX() * 7 / 10); // yaw
                            break;
                        case MotionEvent.ACTION_UP:
                            // s2.SetPosition(1500, s2.InputY(event.getY()));// TODO
                            // REMOVE
                            CH8[1] = 1500; // throttle
                            CH8[2] = 1500; // yaw
                            break;
                    }
                }
                if (app.RadioMode == 4) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            break;
                        case MotionEvent.ACTION_MOVE:
                            // s2.SetPosition((s2.InputX(event.getX())),
                            // s2.InputY(event.getY())); // TODO
                            // REMOVE
                            CH8[3] = (int) s2.InputY(event.getY() * 7 / 10); // throttle
                            CH8[2] = (int) s2.InputX(event.getX() * 7 / 10); // yaw
                            break;
                        case MotionEvent.ACTION_UP:
                            // s2.SetPosition(1500, s2.InputY(event.getY()));// TODO
                            // REMOVE
                            CH8[3] = 1050; // throttle
                            CH8[2] = 1500; // yaw
                            break;
                    }
                }
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        app.ForceLanguage();
        app.Say(getString(R.string.Control));
        killme = false;
        mHandler.postDelayed(update, app.RefreshRate);

        app.mw.SendRequestMSP_MISC();
        app.mw.ProcessSerialData(false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(null);
        killme = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_Connect:
                if (app.CommunicationTypeMW == App.COMMUNICATION_TYPE_BT || app.CommunicationTypeMW == App.COMMUNICATION_TYPE_BT_NEW) {
                    if (!app.MacAddress.equals("")) {
                        app.commMW.Connect(app.MacAddress, app.SerialPortBaudRateMW);
                        app.Say(getString(R.string.menu_connect));
                        startActivity(new Intent(ControlActivity.this, ControlActivity.class));
                        finish();
                    } else {
//                        Toast.makeText(getApplicationContext(), "Wrong MAC address. Go to Config and select correct device", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "错误的MAC地址,设置一下吧！", Toast.LENGTH_LONG).show();
                        SelectBTdevice();
                    }
                    try {
                        mHandler.removeCallbacksAndMessages(null);
                    } catch (Exception e) {
                    }
                } else {
                    app.commMW.Connect(app.MacAddress, app.SerialPortBaudRateMW);
                    app.Say(getString(R.string.menu_connect));
                    startActivity(new Intent(ControlActivity.this, ControlActivity.class));
                    finish();
                }
                break;
            case R.id.bt_QENHight:
                Toast.makeText(getApplicationContext(), "定高中!", Toast.LENGTH_LONG).show();
                CH8[4] = 1500;
                CH8[5] = 1500;
                mHandler.postDelayed(update, app.RefreshRate);
                if (heightStick) {
                    heightStick = false;
                } else {
                    heightStick = true;
                }
                break;
            case R.id.bt_Config:
                finish();
                break;
            case R.id.bt_Unlock:
                if (isLocked) {
                    isLocked = false;//解锁
                    mBtnUnlock.setText(R.string.Lock);
                    Toast.makeText(getApplicationContext(), "解锁中，请等待。", Toast.LENGTH_LONG).show();
                    CH8[3] = 1000;
                    CH8[2] = 1900;
                    mHandler.postDelayed(update, app.RefreshRate);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.currentThread().sleep(500);
                                CH8[2] = 1500;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "解锁失败。我们强烈建议您手动解锁", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).start();
                } else {
                    i++;
                    Toast.makeText(getApplicationContext(), "再次点击锁定飞机", Toast.LENGTH_LONG).show();
                    if (i == 2) {
                        i = 0;
                        isLocked = true;//上锁
                        mBtnUnlock.setText(R.string.Unlock);
                        Toast.makeText(getApplicationContext(), "锁定中，请等待。", Toast.LENGTH_LONG).show();
                        CH8[3] = 1000;
                        CH8[2] = 100;
                        mHandler.postDelayed(update, app.RefreshRate);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.currentThread().sleep(500);
                                    CH8[2] = 1500;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "锁定失败。警报！", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).start();
                    }

                }

                break;
            case R.id.bt_AccCalibration:
                Toast.makeText(getApplicationContext(), "加速度仪校准中，请勿移动飞机", Toast.LENGTH_SHORT).show();
                app.mw.SendRequestMSP_ACC_CALIBRATION();
                break;
            case R.id.bt_HandsMode:
                switch (app.RadioMode) {
                    case MultirotorData.RIGHTHANDSMODE:
                        // 切换为左手油门
                        app.RadioMode = MultirotorData.LEFTHANDSMODE;
                        break;
                    case MultirotorData.LEFTHANDSMODE:
                        // 切换为右手油门
                        app.RadioMode = MultirotorData.RIGHTHANDSMODE;
                        break;
                }
                switch (app.RadioMode) {
                    case MultirotorData.RIGHTHANDSMODE:
                        //右手油门
                        mBtnHandsMode.setText(R.string.Right_hand);
                        break;
                    case MultirotorData.LEFTHANDSMODE:
                        //左手油门
                        mBtnHandsMode.setText(R.string.Left_hand);
                        break;
                }
                break;

        }
    }

    public void SelectBTdevice() {
        Intent serverIntent;
        serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_MULTIWII);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.d(BT_old.TAG, "onActivityResult " + resultCode);
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE_MULTIWII:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    app.MacAddress = address;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "防止误触，点击设置进行退出", Toast.LENGTH_SHORT).show();
    }

}
