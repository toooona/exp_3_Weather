package com.example.weather02;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {
    private Button btn_search;//查找按钮
    private EditText et_search;//通过城市查询天气
    private Button myConcern;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_search = findViewById(R.id.et_search);
        btn_search = findViewById(R.id.btn_search);
        myConcern = findViewById(R.id.concern_text);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(et_search.getText());
                if(searchCountyCode.length() != 6){
                    Toast.makeText(MainActivity.this,"城市ID长度为6位!",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    startActivity(intent);
                }
            }
        });
        myConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyConcernList.class);
                startActivity(intent);
            }
        });
        SharedPreferences pres = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        if (pres.getString("weather",null)!= null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}