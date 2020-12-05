package com.example.weather02.gson;

public class Forecast {
    public String date;
    public String week;
    public Weather weather;
    public Temperature temperature;
    public Wind wind;
    public Power power;

    public class Weather{
        public String dayweather;
        public String nightweather;
    }

    public class Temperature{
        public String daytemp;
        public String nighttemp;
    }

    public class Wind{
        public String daywind;
        public String nightwind;
    }

    public class Power{
        public String daypower;
        public String nightpower;
    }
}
