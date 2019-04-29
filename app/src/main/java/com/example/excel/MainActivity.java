package com.example.excel;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText editText;

    Location location;

    Handler handler = new Handler();

    private GPSUtils gpsUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);

        gpsUtils = new GPSUtils(MainActivity.this);//初始化GPS

        handler.postDelayed(runnable, 0);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            location = gpsUtils.getLocation();//获取位置信息

            if (location != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateView(location);
                    }
                });
                handler.removeCallbacks(runnable);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };


    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if (location != null) {
            editText.setText("定位成功\n\n设备位置信息\n\n经度：");
            editText.append(String.valueOf(location.getLongitude()));
            editText.append("\n纬度：");
            editText.append(String.valueOf(location.getLatitude()));
            editText.append("\n所在地址：");
            editText.append(String.valueOf(gpsUtils.getAddressStr()));
        } else {
            // 清空EditText对象
            editText.getEditableText().clear();
        }
    }
}
