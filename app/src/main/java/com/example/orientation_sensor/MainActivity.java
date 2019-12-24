package com.example.orientation_sensor;




import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
        import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.example.orientation_sensor.PDR.arrayConvert;
import static com.example.orientation_sensor.PDR.calculateAccNorm;
import static com.example.orientation_sensor.PDR.convertMatrix;
import static com.example.orientation_sensor.PDR.convertYaw;
import static com.example.orientation_sensor.PDR.filterConstruction;
import static com.example.orientation_sensor.PDR.findPeaks;
import static com.example.orientation_sensor.PDR.meanCalculation;
import static com.example.orientation_sensor.PDR.string2Double;
import static com.example.orientation_sensor.PDR.string2Long;
import static com.example.orientation_sensor.PDR.varCalculation;
import static com.example.orientation_sensor.Start.oritation;
import static java.lang.Math.pow;

//import java.util.Arrays;
//
//import static com.example.orientation_sensor.PDR.arrayConvert;
//import static com.example.orientation_sensor.PDR.calculateAccNorm;
//import static com.example.orientation_sensor.PDR.convertMatrix;
//import static com.example.orientation_sensor.PDR.convertYaw;
//import static com.example.orientation_sensor.PDR.filterConstruction;
//import static com.example.orientation_sensor.PDR.findPeaks;
//import static com.example.orientation_sensor.PDR.meanCalculation;
//import static com.example.orientation_sensor.PDR.string2Double;
//import static com.example.orientation_sensor.PDR.string2Long;
//import static com.example.orientation_sensor.PDR.varCalculation;

public class MainActivity extends Activity{

    private SensorManager mSensorManager;

    private TextView pitchAngle;
    private TextView rollAngle;
    private TextView azimuthAngle;
    public TextView total_distance;
    private Button next;


    public int start_data=0;
    public int num=0;
    public float a;
    public float b;
    public float c;
    private TextView see;

    private  int count = 1;


    private TextView pitchAngle2;
    private TextView rollAngle2;
    private TextView azimuthAngle2;

    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private TextView azimuth_value;

    private Sensor sensor; //

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    private static final String TAG = "---MainActivity";

    public static float last_oritation;
    public static float now_oritation=0;


    public String[][] acc =new String[120][5];
    public float x;
    public Button big;
    public Button small;
    public static int big_time=0;
    public static int samll_time=0;
    public float scale;
    public double all_distance=0;

    public static long start_time;

    public static int num_realPeakNums;

    public int stepCount;
    public String[][] acc_last =new String[120][5];  //上一次后面的数据

    public int num_last=0; //上一次保留数据的大小
    public String[][] convertedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        azimuthAngle=(TextView) findViewById(R.id.azimuthAngle);
//        pitchAngle=(TextView) findViewById(R.id.pitchAngle);
//        rollAngle=(TextView) findViewById(R.id.rollAngle);

        azimuthAngle2= (TextView) findViewById(R.id.azimuthAngle2);
        pitchAngle2 = (TextView) findViewById(R.id.pitchAngle2);
        rollAngle2 = (TextView) findViewById(R.id.rollAngle2);
        see = (TextView) findViewById(R.id.see_data);
        next = (Button) findViewById(R.id.next);
        big=(Button) findViewById(R.id.big);
        small=(Button) findViewById(R.id.small);
        total_distance= (TextView) findViewById(R.id.total_distance);


        start_time = System.currentTimeMillis();


        // 实例化传感器管理者
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //获取Sensor实例
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


        azimuth_value = (TextView) findViewById(R.id.azimuth_angle_value);
        calculateOrientation();

//下一个界面查看支持哪些传感器
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent=new Intent(MainActivity.this,Sensor_Test.class);//把数据传递到NextActivity
                startActivity(intent);//启动activity

            }
        });
       see.setMovementMethod(ScrollingMovementMethod.getInstance());



        big.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                samll_time=0;
                big_time++;
                Tract tract
                        = (Tract) findViewById(R.id.Tract);
                scale=(float) pow(1.5,big_time);
                tract.setScaleX(scale);
                tract.setScaleY(scale);
                vibrator.vibrate(30);

            }
        });
        small.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                big_time=0;
                samll_time++;
                Tract tract
                        = (Tract) findViewById(R.id.Tract);
                scale=(float) pow(0.9,samll_time);
                tract.setScaleX(scale);
                tract.setScaleY(scale);
                vibrator.vibrate(30);
            }
        });



    }

    void refreshLogView(String msg){


        see.append(msg);
//        int offset=see.getLineCount()*see.getLineHeight();
//        if(offset>see.getHeight()){
//            see.scrollTo(0,offset-see.getHeight());
//        }
    }





    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        // 注册监听
//        mSensorManager.registerListener(new MySensorEventListener(),
//                accelerometer, Sensor.TYPE_ACCELEROMETER);
//        mSensorManager.registerListener(new MySensorEventListener(), magnetic,
//                Sensor.TYPE_MAGNETIC_FIELD);


        mSensorManager.registerListener(new MySensorEventListener(),
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI );

        mSensorManager.registerListener(new MySensorEventListener(),
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);

        super.onResume();
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        // 解除注册
        mSensorManager.unregisterListener(new MySensorEventListener());
        super.onPause();
    }


    // 计算方向
    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null,accelerometerValues,
                magneticFieldValues);  //通过矩阵运算，求出设备的方向
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
//        values[1] = (float) Math.toDegrees(values[1]);
//        values[2] = (float) Math.toDegrees(values[2]);
         x=values[0];
        values[0]=oritation;

        azimuthAngle.setText("确定方向值: "+values[0] );
//        pitchAngle.setText("前后翻转角度: "+values[1] );
//        rollAngle.setText("左右翻转角度: "+values[2] );

        // Log.i(TAG, values[0] + "");
        if (values[0] >= -5 && values[0] < 5) {
            azimuth_value.setText("正北");

        } else if (values[0] >= 5 && values[0] < 85) {
            // Log.i(TAG, "东北");
            azimuth_value.setText("东北");
        } else if (values[0] >= 85 && values[0] <= 95) {
            // Log.i(TAG, "正东");
            azimuth_value.setText("正东");
        } else if (values[0] >= 95 && values[0] < 175) {
            // Log.i(TAG, "东南");
            azimuth_value.setText("东南");
        } else if ((values[0] >= 175 && values[0] <= 180)
                || (values[0]) >= -180 && values[0] < -175) {
            // Log.i(TAG, "正南");
            azimuth_value.setText("正南");
        } else if (values[0] >= -175 && values[0] < -95) {
            // Log.i(TAG, "西南");
            azimuth_value.setText("西南");
        } else if (values[0] >= -95 && values[0] < -85) {
            // Log.i(TAG, "正西");
            azimuth_value.setText("正西");
        } else if (values[0] >= -85 && values[0] < -5) {
            // Log.i(TAG, "西北");
            azimuth_value.setText("西北");
        }
    }
    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            count++;


//            int sensorType = event.sensor.getType();
//            int date = 0;
//            switch (sensorType){
//                case Sensor.TYPE_ACCELEROMETER:
//                    accelerometerValues = event.values;
//                    a = event.values[0];
//                    b = event.values[1];
//                    c = event.values[2];
//                    azimuthAngle2.setText("azim_angle2: "+a );
//                    pitchAngle2.setText("pitch_angle2: "+b );
//                    rollAngle2.setText("roll_angle2: "+c);
//                    break;
//                case Sensor.TYPE_MAGNETIC_FIELD:
//                    magneticFieldValues = event.values;
//                    break;
//            }
//            calculateOrientation();



           //  TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {   //加速度传感器
                accelerometerValues = event.values;

            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {  //磁场传感器
                magneticFieldValues = event.values;
            }
            calculateOrientation();
            a = accelerometerValues[0];
            azimuthAngle2.setText("Z: "+a );
            b =  accelerometerValues[1];
            pitchAngle2.setText("X: "+b );
            c =  accelerometerValues[2];
            rollAngle2.setText("Y: "+c);

           // Log.e(TAG, ""+1000 / ((System.currentTimeMillis() - MainActivity.start_time) / count));  //查看每秒采样频率

//            Log.e(TAG, "last_oritation"+last_oritation );
//            Log.e(TAG, "oritation"+oritation );
//        if(start_data==1) {
//            if (Math.abs(last_oritation - oritation) > 20) {
//                now_oritation = 1;
//            }
//        }

                acc[num][0]=String.valueOf(System.currentTimeMillis());
                acc[num][1]=String.valueOf(a);
                acc[num][2]=String.valueOf(b);
                acc[num][3]=String.valueOf(c);
                acc[num][4]=String.valueOf(oritation);


            //去掉初始的10个点
            num++;
            if((num==10) && (start_data==0))
            {
                start_data=1;
                num=0;
            }

            //每300个点进行一次计算
            if(  (num==120)  )
            {
                if(acc_last != null)
                {
                    Log.e(TAG,"ssssssssssssssssssssssssssssssssssssssssss" );
                    String[][] acc_total =new String[num_last+120][5];
                    int i=0;
                    for(i=0;i<num_last;i++)
                    {
                        acc_total[i][0]= acc_last[i][0];
                        acc_total[i][1]= acc_last[i][1];
                        acc_total[i][2]= acc_last[i][2];
                        acc_total[i][3]= acc_last[i][3];
                        acc_total[i][4]= acc_last[i][4];
                    }

                    for(int j=0; j<120;j++)
                    {
                        acc_total[i][0]= acc[j][0];
                        acc_total[i][1]= acc[j][1];
                        acc_total[i][2]= acc[j][2];
                        acc_total[i][3]= acc[j][3];
                        acc_total[i][4]= acc[j][4];
                        i++;
                    }
                    convertedData = convertMatrix(acc_total);
                }
                else
                {
                    convertedData = convertMatrix(acc);
                }

                // 转置后的矩阵，0行为时间，3-5行为ACC，6-8行为gyr，9-11行为磁力计，12-14行为yaw, pitch, roll

                String[][] accData =  Arrays.copyOfRange(convertedData, 1, 4);  //数据
                String[][] index = Arrays.copyOfRange(convertedData, 0, 1);    //时间
                String[][] strYaw = Arrays.copyOfRange(convertedData, 4, 5);   //oritation


                index = convertMatrix(index);
                index = Arrays.copyOfRange(index, 0, index.length);
                long[] indexLong = arrayConvert(string2Long(index));           // indexLong为long型的时间轴



                strYaw = convertMatrix(strYaw);
                strYaw = Arrays.copyOfRange(strYaw, 0, strYaw.length);
                double[] orientation = arrayConvert(string2Double(strYaw));


                //accData  是3*N   3行N列
                accData = convertMatrix(accData);
                accData = Arrays.copyOfRange(accData, 0, accData.length);
                double[][] accDoubleData = string2Double(accData);              // double型的acc数据


                //从这里开始算------------------------------------------------------------------------------------------------------------------------
                double[][] finalYaw = convertYaw(orientation);          // 第一行为原始值，第二行为sin值，第三行为cos值
                double[] accNorm = calculateAccNorm(accDoubleData);    //   N*3
                double[] filteredAccNorm = filterConstruction(accNorm); // 到这一步都正确

//                for(int i=0;i<10;i++)
//                {
//                    Log.e(TAG,"*******"+ accNorm[i] );
//                }


                double[][] peaksInfo = findPeaks(filteredAccNorm, indexLong);   // 3行分别为位置编号，峰值，时间差
                Log.e(TAG, "peaksInfo[0].length peaksInfo[0].length peaksInfo[0].length -----------------------------" + peaksInfo[0].length );
                //保留最后一个峰值的数据到下一次数组
                if(peaksInfo[0].length > 1) {


                    stepCount = peaksInfo[0].length-1;


                    int j=0;
                    for( double i= peaksInfo[0][peaksInfo[0].length-1] -5; i<120;   i++  )
                    {
                        acc_last[j][0]= acc[(int)i][0];
                        acc_last[j][1]= acc[(int)i][1];
                        acc_last[j][2]= acc[(int)i][2];
                        acc_last[j][3]= acc[(int)i][3];
                        acc_last[j][4]= acc[(int)i][4];
                        j++;
                        num_last=j-1;
                    }


                }
                else {
                     stepCount = peaksInfo[0].length;
                }

                double[] positionX = new double[stepCount];
                double[] positionY = new double[stepCount];
                double distance = 0.0;
                double stepFreq, stepAV, stepLength = 0;
                int posStart, posEnd;
                double yawSin, yawCos;


                for (int i=0; i<stepCount-1; i++){
                    posStart = (int)peaksInfo[0][i];
                    posEnd = (int)peaksInfo[0][i+1];

                   // Log.e(TAG, "peaksInfo[2][i+1]-----------------------------" + peaksInfo[2][i+1]);

                    yawSin = meanCalculation(posStart, posEnd, finalYaw[1]);
                    yawCos = meanCalculation(posStart, posEnd, finalYaw[2]);
                    stepFreq = 1000.0 / peaksInfo[2][i+1];
                    stepAV = varCalculation(posStart, posEnd, accNorm);
                   // stepLength = 0.2844 + 0.2231*stepFreq + 0.0426*stepAV;
                    stepLength = 0.2844 + 0.2531*stepFreq + 0.0526*stepAV;


                   // Log.e(TAG, "stepFreq-----------------------------" +stepFreq);

                    distance = distance + stepLength;
                    positionX[i+1] = positionX[i] + stepLength * Math.cos(Math.toRadians(yawCos));
                    positionY[i+1] = positionY[i] + stepLength * Math.sin(Math.toRadians(yawSin));

                    System.out.println("X--------------为: "+positionX[i+1] );
                    System.out.println("Y--------------为: "+positionY[i+1] );
                    refreshLogView("X为: "+positionX[i+1] + '\n');
                    refreshLogView("Y为: "+positionY[i+1] + '\n');


                        Tract tract
                                = (Tract) findViewById(R.id.Tract);
                       //tract.getnum((int) Math.round(positionY[i + 1]), (int) Math.round(positionX[i + 1]));
                        tract.getnum((float) positionY[i + 1], (float) positionX[i + 1]);
                }

                //System.out.println("距离为：" + distance);

                all_distance=all_distance+distance;
                total_distance.setText("总距离为:" + (float)all_distance);

                refreshLogView("距离为: "+distance  + '\n');
                refreshLogView("------------------------- " + '\n');
                num=0;
                now_oritation=0;


            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }




}