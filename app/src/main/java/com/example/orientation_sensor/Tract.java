/**
 * Created by 陈泳吉 on 2019-05-07.
 */
package com.example.orientation_sensor;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Math.pow;


/**
 * Created by 陈泳吉 on 2019-05-07.
 */



public class Tract extends View {

    private final static String TAG = Tract.class.getSimpleName();
    @SuppressLint("HandlerLeak")

    private boolean mIsDrawGird = true;                    // 是否画网格
    private Paint paint;
    private Paint electrocarPaint;                         // 画心电图曲线的画笔
    private Path electrocarPath;

    private Paint linePaint;


    public float a;
    public float b;

    public  int num2=0;

    public int  aa=1;
    public static int linshi_x=0;
    public static int linshi_y=0;

    private int width;
    private int height;


    private int widthOfSmallGird;                          // 小格子的宽度

    private List<Float> datas_x = new ArrayList<>();  //定义缓存list
    private List<Float> datas_y = new ArrayList<>();  //定义缓存list


    public int interval=200; //放缩的间距大小 200
    public int time=1;   //放缩的次数

    Tract tract = this;
    public float scale;

    private  float last_x = 0;
    public float last_y =0;

    public int num3 = 1;

    private  float nowData_x = 0;
    private  float nowData_y=0;

    public int o_x=60000;
    public int o_y=60000;

    private int re_draw=0; //屏幕画满后清除移动初始画线点判断

    public int num0 = 0; //数组的元素序号
    public int ini_data = 0; //姿态变换后，判断是否前一个姿态是画心电图，如果是画心电图，则需要清除画布
    public static float draw_data = 0;   //存放画图位



    public Tract(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Tract(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {

        paint = new Paint();                              //画网格的 Paint
        paint.setStyle(Paint.Style.STROKE);
        electrocarPaint = new Paint();                    //画心电图曲线的 Paint
        electrocarPaint.setColor(Color.BLACK);
        electrocarPaint.setStyle(Paint.Style.STROKE);
        electrocarPaint.setAntiAlias(true);               //抗锯齿
        electrocarPaint.setStrokeWidth(2);
        electrocarPath = new Path();                      //心电图的轨迹 Path


        linePaint = new Paint();                    //画心电图曲线的 Paint
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);               //抗锯齿
        linePaint.setStrokeWidth(1);

//        tract.setScaleX(2.0f);
//        tract.setScaleY(2.0f);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure");

        width=getMeasuredWidth();
        height=getMeasuredHeight();
        //screenWidth= ScreenUtil.getScreenWidth(context);
        // screenHeight=ScreenUtil.getScreenHeight(context)-getStatusBarHeight();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e(TAG, "onSizeChanged");

        widthOfSmallGird = 18;
        width = w;     //竖屏1080  横屏1920
        height = h;  //=810
        setData();                            // 设置数据


        a=tract.getHeight();
        b=tract.getWidth();
        Log.e(TAG,a+ "aaaaaaaaaaaaaaaaaaaaaaaaaa");
        Log.e(TAG,b+ "bbbbbbbbbbbbbbbbbbbbbbbbbb");


    }
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw");

        canvas.translate(60000, 60000); //设定原点位置
        //scrollBy(-1500, 0); //移动图像
        //canvas.drawCircle(0, 0, 100, electrocarPaint);
       // canvas.drawCircle(0,0,200,electrocarPaint);
//        canvas.drawCircle(0,0,300,electrocarPaint);
//        canvas.drawCircle(0,0,400,electrocarPaint);
        //canvas.drawLine(-6000,0,6000,0,electrocarPaint);
        //canvas.drawLine(-0,-60000,0,6000,electrocarPaint);
        canvas.drawLine(0,0,getWidth(),1,linePaint);
        canvas.drawLine(0,0,1,getWidth(),linePaint);
        canvas.drawLine(0,0,1,-getHeight(),linePaint);
        canvas.drawLine(0,0,-getHeight(),1,linePaint);
        if (aa == 1)
        {
            this.setScaleX(3);
            this.setScaleY(3);
            aa=0;
        }

        drawElectrocardiogram(canvas);        // 画心电图

    }

    /**
     * 画心电图曲线
     */
    private void drawElectrocardiogram(Canvas canvas) {


        last_x=last_x+nowData_x;
        last_y=last_y+nowData_y*(-1);

        electrocarPath.lineTo(last_x,last_y);
        electrocarPath.moveTo(last_x, last_y);
        canvas.drawPath(electrocarPath, electrocarPaint);
//        o_x=o_x+last_x;
//        o_y=o_y+last_y;
//        Log.e(TAG, o_x + "-----last_xlast_xlast_xlast_xlast_x");
//        Log.e(TAG, o_y + "-----last_ylast_ylast_ylast_ylast_y");

        if((last_x>interval) ||  (last_x<-interval)  || (last_y>interval) ||(last_y<-interval) ){
            scale=(float) pow(0.9,time);
            this.setScaleX(scale);
            this.setScaleY(scale);

            //canvas.scale(0.5f,0.5f);
            invalidate();
            time++;
            interval=200*time;
        }


//             }

    }


    /**
     * 增加数据，使心电图呈现由左到右显示出波形的效果
     */
    public  void addData() {
        if(datas_x.size() >= 0) {
            // datas 是收集到的数据， electrocardDatas 是显示在屏幕的数据，两者都是 ArrayList<Float>
            generateElectrocar();
            invalidate();


        }
    }

    public  void startDraw() {
        addData();
    }


    public void setData() {
        generateElectrocar();

    }


    public  void getnum(float num1, float num2)
    {
        nowData_x = num1;
        nowData_y = num2;

        startDraw();

    }



    //获取数据并转换
    public  void generateElectrocar() {
        datas_x.add(nowData_x);
        datas_y.add(nowData_y);
        Log.e(TAG, nowData_x+ "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        Log.e(TAG, nowData_y+ "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");

    }


    private float downX;
    private float downY;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();

                    break;
//                case MotionEvent.ACTION_MOVE:
//                    final float xDistance = event.getX() - downX;
//                    final float yDistance = event.getY() - downY;
//                    int l,r,t,b;
//                    //当水平或者垂直滑动距离大于10,才算拖动事件
//                    if (Math.abs(xDistance) >50 ||Math.abs(yDistance)>50) {
//
//                        l = (int) (getLeft()+ xDistance*scale);
//                        r = l+width;
//                        t = (int) (getTop()+ yDistance*scale);
//                        b = t+height;
//                        this.layout(l, t, r, b);
//                    }
//                    break;
                case MotionEvent.ACTION_UP:
                    final float xDistance = event.getX() - downX;
                    final float yDistance = event.getY() - downY;
                    int l,r,t,b;
                    //当水平或者垂直滑动距离大于10,才算拖动事件
                    if (Math.abs(xDistance) >50 ||Math.abs(yDistance)>50) {
                        l = (int) (getLeft()+ xDistance);
                        r = l+width;
                        t = (int) (getTop()+ yDistance);
                        b = t+height;

                        this.layout(l, t, r, b);
                    }

                    setPressed(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    setPressed(false);
                    break;
            }
            return true;
        }
        return false;
    }



}