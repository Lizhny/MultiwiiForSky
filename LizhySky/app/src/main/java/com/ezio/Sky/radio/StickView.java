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
package com.ezio.Sky.radio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ezio.Sky.helpers.Functions;

public class StickView extends View {

    public float x, y;
    int hh, ww;

    Paint paint = new Paint();
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();

    float scaledDensity = 0;

    public float InputX(float x) {
        float a = Functions.map(x, 0, ww, 1000, 2000);
//        if (a > 2000)
//            a = 2000;
        if (a < 1000)
            a = 1000;
        return a;
    }

    public float InputY(float y) {
        float a = Functions.map(hh - y, 0, ww, 1000, 2000);
//        if (a > 2000)
//            a = 2000;
        if (a < 1000)
            a = 1000;
        return a;

    }

    public void SetPosition(float xx, float yy) {//根据舵量值绘制摇杆的位置
        x = (ww / 2) + ((xx - 1500) / 500) * (ww / 2);
        y = (hh / 2) - ((yy - 1500) / 500) * (hh / 2);
        invalidate();//请求重新draw()，但只会绘制调用者本身

    }

    public StickView(Context context) {
        super(context);

        init();
    }

    public StickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        //三画笔
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);

        paint1.setAntiAlias(false);
        paint1.setColor(Color.BLACK);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(3);

        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.FILL);

        paint3.setColor(Color.YELLOW);
        paint3.setStyle(Paint.Style.STROKE);

        scaledDensity = getResources().getDisplayMetrics().scaledDensity;//获取屏幕分辨率、宽高

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        ww = w / 10 * 7;
        hh = h / 10 * 7;
        super.onSizeChanged(w, h, oldw, oldh);
        SetPosition(1500, 1500);

    }

    @Override
    protected void onDraw(Canvas canvas) {

//        canvas.drawColor(Color.TRANSPARENT);
//        float a = 40;
//        RectF rectF1 = new RectF(1, 1, ww - 1, hh - 1);
//        canvas.drawRoundRect(rectF1,a,a, paint2);
//        canvas.drawCircle(ww / 2 + 150, hh / 2 + 250, ww / 2, paint2);//大灰圆
        //黄色十字线
        canvas.drawLine(150, hh / 2 + 250, ww + 150, hh / 2 + 250, paint3);
        canvas.drawLine(ww / 2 + 150, (hh - ww) / 2 + 250, ww / 2 + 150, (hh + ww) / 2 + 250, paint3);

        canvas.drawCircle(ww / 2 + 150, hh / 2 + 250, 5 * scaledDensity, paint1);//绘制中间空心圆
        canvas.drawCircle(ww / 2 + 150, hh / 2 + 250, 200, paint1);//绘制第二圈园，好看~和定位
        canvas.drawCircle(ww / 2 + 150, hh / 2 + 250, ww / 2 - 3, paint1);//绘制第三圈园，好看~和定位
        canvas.drawCircle(ww / 2 + 150, hh / 2 + 250, ww / 2, paint1);//绘制最外圈园，好看~和定位

        //虚拟摇杆横线
        canvas.drawLine(ww / 2 - 5 * scaledDensity + 150, hh / 2 + 250, x - 10 * scaledDensity + 150, y + 250, paint2);
        canvas.drawLine(ww / 2 + 5 * scaledDensity + 150, hh / 2 + 250, x + 10 * scaledDensity + 150, y + 250, paint2);
        //虚拟摇杆竖线
//        canvas.drawLine(ww / 2 + 150, hh / 2 - 5 * scaledDensity + 250, x+150, y - 10 * scaledDensity+250, paint1);
//        canvas.drawLine(ww / 2 + 150, hh / 2 + 5 * scaledDensity + 250, x+150, y + 10 * scaledDensity+250, paint1);

        canvas.drawCircle(x + 150, y + 250, 10 * scaledDensity, paint);//绘制摇杆顶部绿色
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