package com.example.weather02.util;

import android.text.TextUtils;

import com.example.weather02.gson.Now;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.weather02.db.City;
import com.example.weather02.db.County;
import com.example.weather02.db.Province;

public class Utility {

    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinceAll = new JSONArray("districts");
                for (int j = 0; j < provinceAll.length(); j++) {
                    JSONObject provinceObject = provinceAll.getJSONObject(j);
                    Province provinceN = new Province();
                    provinceN.setProvinceCode(provinceObject.getString("adcode"));
                    provinceN.setProvinceName(provinceObject.getString("name"));
                    provinceN.save();
                }
                return true;
            }
            catch(JSONException e){
                e.printStackTrace();
            }

        }
        return false;
    }

    public static boolean handleCityResponse(String response, String provinceCode){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray provinceAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < provinceAll.length(); i++) {
                    JSONObject province1 = provinceAll.getJSONObject(i);
                    //插入市
                    JSONArray cityAll = province1.getJSONArray("districts");
                    for (int j = 0; j < cityAll.length(); j++) {
                        JSONObject city2 = cityAll.getJSONObject(j);
                        String adcode2 = city2.getString("adcode");
                        String name2 = city2.getString("name");
                        City cityN = new City();
                        cityN.setCityCode(adcode2);
                        cityN.setCityName(name2);
                        cityN.setProvinceCode(provinceCode);
                        cityN.save();
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, String cityCode){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray cityAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < cityAll.length(); i++) {
                    JSONObject city2 = cityAll.getJSONObject(i);
                    //插入市
                    JSONArray countyAll = city2.getJSONArray("districts");
                    for (int j = 0; j < countyAll.length(); j++) {
                        JSONObject county3 = countyAll.getJSONObject(j);
                        String adcode3 = county3.getString("adcode");
                        String name3 = county3.getString("name");
                        County countyN = new County();
                        countyN.setCountyCode(adcode3);
                        countyN.setCountyName(name3);
                        countyN.setCityCode(cityCode);
                        countyN.save();
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }



    public static Now handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String s = jsonObject.getString("status");
            JSONArray jsonArray = jsonObject.getJSONArray("lives");
            //获取“live”Json数组
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject o = jsonArray.getJSONObject(i);
                String weatherContent = o.toString();
                Now weather = new Gson().fromJson(weatherContent, Now.class);
                weather.setStatus(s);
                return weather;
            } }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

