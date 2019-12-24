package com.example.orientation_sensor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Sensor_Test extends Activity {

    private SensorManager mSensorManager;
    private ListView sensorListView;
    private List<Sensor> sensorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        sensorListView = (ListView) findViewById(R.id.lv_all_sensors);
        // 实例化传感器管理者
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 得到设置支持的所有传感器的List
        sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String> sensorNameList = new ArrayList<String>();
        for (Sensor sensor : sensorList) {
            sensorNameList.add(sensor.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sensorNameList);
        sensorListView.setAdapter(adapter);
    }

}
