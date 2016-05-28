package com.lizhy.Sky.radio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.lizhy.Sky.helpers.Functions;

public class GravityView extends View {

	public float x, y;
	int hh, ww;

	Paint paint = new Paint();
	Paint paint1 = new Paint();
	Paint paint2 = new Paint();
	Paint paint3 = new Paint();

	float scaledDensity = 0;

	public float InputX(float x) {
		float a = Functions.map(x, 0, ww, 1000, 2000);
		if (a > 2000)
			a = 2000;
		if (a < 1000)
			a = 1000;
		return a;
	}

	public float InputY(float y) {
		float a = Functions.map(hh - y, 0, ww, 1000, 2000);
		if (a > 2000)
			a = 2000;
		if (a < 1000)
			a = 1000;
		return a;

	}

	public void SetPosition(float xx, float yy) {// 根据舵量值绘制摇杆的位置
		x = (ww / 2) + ((xx - 1500) / 500) * (ww / 2);
		y = (hh / 2) - ((yy - 1500) / 500) * (hh / 2);
		invalidate();// 请求重新draw()，但只会绘制调用者本身

	}

	public GravityView(Context context) {
		super(context);

		init();
	}

	public GravityView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {

		paint.setAntiAlias(true);// 设置paint为无锯齿
		paint.setColor(Color.GREEN);// 设置颜色为绿
		paint.setStyle(Paint.Style.FILL);

		paint1.setAntiAlias(true);
		paint1.setColor(Color.BLACK);       //设置颜色为黑
		paint1.setStyle(Paint.Style.STROKE);// 设置为中空的样式

		paint2.setColor(Color.GRAY);// 设置颜色为灰色
		paint2.setStyle(Paint.Style.FILL);

		paint3.setColor(Color.YELLOW);//设置颜色为黄色
		paint3.setStyle(Paint.Style.FILL);

		scaledDensity = getResources().getDisplayMetrics().scaledDensity;// 获取屏幕分辨率、宽高

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		ww = w;
		hh = h;
		super.onSizeChanged(w, h, oldw, oldh);
		SetPosition(1500, 1500);

	}

	// 画位图,ww为区域宽(x方向)，hh对应区域高(y方向);x,y为触控点。注意！所有这些以横屏建立坐标系。
	@Override
	protected void onDraw(Canvas canvas) {
		float a=40;
		canvas.drawColor(Color.TRANSPARENT);//设置画布的背景色为透明
		RectF rectF1 = new RectF(1, 1, ww - 1, hh - 1);

		canvas.drawRoundRect(rectF1,a,a, paint2);
//		canvas.drawRoundRect(RectF, float, float, Paint) 方法用于画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。

		canvas.drawLine(0, hh / 2, ww, hh / 2, paint3);
//		canvas.drawLine(startX, startY, stopX, stopY, paint)：前四个参数的类型均为float，最后一个参数类型为Paint。表示用画笔paint从点（startX,startY）到点（stopX,stopY）画一条直线；
		canvas.drawLine(ww / 2, 0, ww / 2, hh, paint3);
//		canvas.drawArc(oval, startAngle, sweepAngle, useCenter, paint)：第一个参数oval为RectF类型，即圆弧显示区域，startAngle和sweepAngle均为float类型，分别表示圆弧起始角度和圆弧度数,3点钟方向为0度，useCenter设置是否显示圆心，boolean类型，paint为画笔；
		canvas.drawCircle(ww / 2, hh / 2, 5 * scaledDensity, paint1);// 在中心为(ww/2,hh/2)的地方画个半径为5*scaledDensity的圆，宽度为setStrokeWidth；

		canvas.drawLine(ww / 2 - 5 * scaledDensity, hh / 2, x - 15
				* scaledDensity, y, paint1);
		canvas.drawLine(ww / 2 + 5 * scaledDensity, hh / 2, x + 15
				* scaledDensity, y, paint1);

		canvas.drawLine(ww / 2, hh / 2 - 5 * scaledDensity, x, y - 15
				* scaledDensity, paint1);
		canvas.drawLine(ww / 2, hh / 2 + 5 * scaledDensity, x, y + 15
				* scaledDensity, paint1);

		canvas.drawCircle(x, y, 15 * scaledDensity, paint);

	}



	@Override
	protected void onAttachedToWindow() {

		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {

		super.onDetachedFromWindow();
	}
}
