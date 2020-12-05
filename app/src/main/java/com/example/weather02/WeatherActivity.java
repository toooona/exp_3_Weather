package com.example.weather02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import com.bumptech.glide.Glide;
import com.example.weather02.db.County;
import com.example.weather02.db.Follow;
import com.example.weather02.gson.Now;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.weather02.service.AutoUpdateService;
import com.example.weather02.util.HttpUtil;
import com.example.weather02.util.Utility;

import org.litepal.LitePal;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button concern;
    private Button concealConcern;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private ImageView bingPicImg;
    private TextView provinceText;//省区
    private TextView cityText;//市区
    private TextView weatherText;//天气
    private TextView temperatureText;//温度
    private TextView humidityText;//湿度
    private TextView reportTimeText;//时间

    private Button myConcern;
    private Button btn_search;//查找按钮
    private EditText et_search;//通过城市查询天气



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //活动布局显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        provinceText = findViewById(R.id.province_text);
        cityText = findViewById(R.id.city_text);
        weatherText = findViewById(R.id.weather_text);
        temperatureText = findViewById(R.id.temperature_text);
        humidityText = findViewById(R.id.humidity_text);
        reportTimeText = findViewById(R.id.reporttime_text);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        concern = findViewById(R.id.concern);
        concealConcern = findViewById(R.id.concealConcern);

        myConcern = findViewById(R.id.concern_text);
        et_search = findViewById(R.id.et_search);
        btn_search = findViewById(R.id.btn_search);

        SharedPreferences prefs = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        String adcodeString = prefs.getString("weather",null);
        final String countyCode;
        final String countyName;
        if (adcodeString != null) {
            Now weather = Utility.handleWeatherResponse(adcodeString);
            countyCode = weather.adcode;
            countyName = weather.city;
            showWeatherInfo(weather);
        } else {
            countyCode = getIntent().getStringExtra("adcode");
            countyName = getIntent().getStringExtra("city");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyCode);
        }

        final String x = cityText.getText().toString();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){//下拉进度条监听器
            @Override
            public void onRefresh() {   //回调
                requestWeather(countyCode);//回调方法
            }
        });

        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
                //打开滑动菜单
            }
        });


        concern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LitePal.where("city_code = ?", countyCode).find(Follow.class).size() != 0)
                    Toast.makeText(WeatherActivity.this, "已关注此城市！", Toast.LENGTH_SHORT).show();
                else{
                    Follow follow = new Follow();
                    follow.setCity_code(countyCode);
                    follow.setCity_name(countyName);
                    follow.save();
                    Toast.makeText(WeatherActivity.this, "关注成功！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        concealConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LitePal.where("city_code = ?", countyCode).find(Follow.class).size() == 0)
                    Toast.makeText(WeatherActivity.this, "未关注此城市！", Toast.LENGTH_SHORT).show();
                else{
                    LitePal.deleteAll(Follow.class, "city_code=?", countyCode);
                    Toast.makeText(WeatherActivity.this, "取消关注成功！", Toast.LENGTH_LONG).show();
                }
            }
        });

        myConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, MyConcernList.class);
                startActivity(intent);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(et_search.getText());
                if(searchCountyCode.length() != 6){
                    Toast.makeText(WeatherActivity.this,"城市ID长度为6位!",Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(WeatherActivity.this,WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    startActivity(intent);
                }
            }
        });

        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

    }

    public void requestWeather(final String adCode) {
        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adCode + "&key=194e271c1d68588e69f8c27901715819";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Now weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "1".equals(weather.status)) {
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,城市ID不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        swipeRefresh.setRefreshing(false);
                        //刷新事件结束
                    }
                });
                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        //刷新事件结束
                    }
                });
            }
        });
        loadBingPic();
        //每次请求天气信息的同时刷新背景图片
    }

   private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Now now) {
        String provinceName = now.province;
        String cityName = now.city;
        String weatherName = now.weather;
        String temperatureName = now.temperature;
        String humidityName = now.humidity;
        String reportTime = now.reporttime;
        provinceText.setText(provinceName);
        cityText.setText(cityName);
        weatherText.setText(weatherName);
        temperatureText.setText(temperatureName + "℃");
        humidityText.setText("湿度:" + humidityName + "%");
        reportTimeText.setText(reportTime);
        weatherLayout.setVisibility(View.VISIBLE);

//        Intent intent = new Intent(this, AutoUpdateService.class);
//        startService(intent);
    }
}