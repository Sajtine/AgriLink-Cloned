package com.example.loginappclone;

import java.io.Serializable;
import java.util.List;

public class WeatherResponse implements Serializable {
    public Main main;
    public List<Weather> weather;
    public Wind wind;
    public Rain rain;
    public int visibility;
    public long dt;

    public static class Main implements Serializable{
        public float temp;
        public float feels_like;
        public float temp_min;
        public float temp_max;
        public int pressure;
        public int humidity;

    }

    public static class Weather implements Serializable{
        public String main;
        public String description;
        public String icon;
    }

    public static class Wind implements Serializable{
        public float speed;
        public float deg;

    }

    public static class Rain implements Serializable{
        public float _1h;
    }
}
