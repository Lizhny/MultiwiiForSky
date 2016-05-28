package com.lizhy.Sky.advanced;

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

/*舵量控制函数
 * 包含屏幕触控函数，将参数传递给手机屏幕绘图函数和飞控传递函数处理*/
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import com.lizhy.Sky.R;
import com.lizhy.Sky.app.App;
import com.lizhy.Sky.radio.GravityView;

public class GravitySensorControl extends Activity implements SensorListener{
	int p, r;
	private boolean killme = false;
	App app;
	Handler mHandler = new Handler();

	GravityView s3;

	int[] CH8 = { 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500 };

	@SuppressWarnings("deprecation")//注解@SuppressWarnings用来压制程序中出来的警告，比如在没有用泛型或是方法已经过时的时候

	final String tag = "IBMEyes";
	SensorManager sm = null;
	int pit,rol;



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

			s3.SetPosition(app.mw.rcYaw, app.mw.rcThrottle);

			// * 0rcRoll 1rcPitch 2rcYaw
			// 3rcThrottle 4rcAUX1 5rcAUX2
			// 6rcAUX3 7rcAUX4
			//

			// app.mw.SendRequestSetRawRC(CH8);
			app.Frequentjobs();

			// app.mw.SendRequest();
			app.mw.SendRequestMSP_SET_RAW_RC(CH8);

			if (!killme)
				mHandler.postDelayed(update, app.RefreshRate);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		app = (App) getApplication();
		setContentView(R.layout.gravitysensor_control);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 强制成竖屏

		s3 = (GravityView) findViewById(R.id.gravityview);}
	public void onSensorChanged(int sensor, float[] values) {
		synchronized (this) {
			Log.d(tag, "onSensorChanged: " + sensor + ", x: " + values[0]
					+ ", y: " + values[1] + ", z: " + values[2]);
//				if (sensor == SensorManager.SENSOR_ORIENTATION) {
//					
//				}
		}
//				if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
		pit=(int) values[1];//pitch
		rol=(int) values[0];//roll
//				}
		p = (((pit) * 166) + 1500);
		r = (((rol) * 166) + 1500);
		CH8[1] = p; // pitch
		CH8[0] = r;// roll

		s3.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					// 触摸按下的事件
					case MotionEvent.ACTION_DOWN:

						break;
					// 触摸移动的事件
					case MotionEvent.ACTION_MOVE:
						// s3.SetPosition((s1.InputX(event.getX())),
						// s3.InputY(event.getY())); // TODO
						// REMOVE
						CH8[3] = (int) s3.InputY(event.getY()); // throttle
						CH8[2] = (int) s3.InputX(event.getX()); // yaw
						break;
					// 触摸抬起的事件
					case MotionEvent.ACTION_UP:
						// s3.SetPosition(1500, s3.InputY(event.getY()));// TODO
						// REMOVE
						CH8[3] = 0; // throttle
						CH8[2] = 1500; // yaw
						break;
				}

				return true;
			}

		});

	}
	public void onAccuracyChanged(int sensor, int accuracy) {
		Log.d(tag, "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);

	}


	@Override
	protected void onResume() {
		super.onResume();
		app.ForceLanguage();
		app.Say(getString(R.string.GravitySensorControl));
		killme = false;
		mHandler.postDelayed(update, app.RefreshRate);
		sm.registerListener(this, SensorManager.SENSOR_ORIENTATION
						| SensorManager.SENSOR_ACCELEROMETER,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(null);
		killme = true;
	}


	@Override
	protected void onStop() {
		sm.unregisterListener(this);
		super.onStop();
	}

}