package com.example.weather02;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weather02.db.Follow;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;



public class MyConcernList extends AppCompatActivity {
    ArrayAdapter simpleAdapter;
    ListView MyConcernList;
    private List<String> city_nameList = new ArrayList<>();
    private List<String> city_codeList = new ArrayList<>();

    private void InitConcern() {       //进行数据填装
        List<Follow> follows = LitePal.findAll(Follow.class);
        for(Follow f:follows){
            String city_code = f.getCity_code();
            String city_name = f.getCity_name();
            city_codeList.add(city_code);
            city_nameList.add(city_name);
        }
    }

    public void RefreshList(){
        city_nameList.removeAll(city_nameList);
        city_codeList.removeAll(city_codeList);
        simpleAdapter.notifyDataSetChanged();
        InitConcern();
    }

    @Override
    protected void onStart(){
        super.onStart();
        RefreshList();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myconcern_list);
        MyConcernList = findViewById(R.id.MyConcernList);
//        LitePal.deleteAll(Follow.class);

        InitConcern();

        simpleAdapter = new ArrayAdapter(MyConcernList.this,android.R.layout.simple_list_item_1, city_nameList);

        MyConcernList.setAdapter(simpleAdapter);
        MyConcernList.setOnItemClickListener(new AdapterView.OnItemClickListener(){      //配置ArrayList点击按钮
            @Override
            public void  onItemClick(AdapterView<?> parent, View view , int position , long id){
                String tran = city_codeList.get(position);
                Intent intent = new Intent(MyConcernList.this, WeatherActivity.class);
                intent.putExtra("adcode",tran);
                startActivity(intent);
            }
        });

    }
}
