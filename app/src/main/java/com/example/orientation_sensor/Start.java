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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.BatchUpdateException;

import static java.lang.Math.pow;

public class Start extends Activity implements SensorEventListener{

    public Button start;
    private SensorManager sensorManager;
    private Sensor sensor;
    public static float oritation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);

        start=(Button )findViewById(R.id.start);

//获取SensorManager实例
 sensorManager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//获取Sensor实例
 sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//注册滚动事件
sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);



        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent=new Intent(Start.this,MainActivity.class);//把数据传递到NextActivity
                startActivity(intent);//启动activity


            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      // System.out.println("````````````为：" + event.values[0]);
        oritation=event.values[0];
    }

    @Override
 public void onAccuracyChanged(Sensor sensor, int i) {


 }


 @Override
protected void onDestroy() {
 super.onDestroy();
sensorManager.unregisterListener(this);
 }
}
