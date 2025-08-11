package com.example.loginappclone;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ForecastResponse implements Serializable {

    public List<ForecastItem> list;

    public static class ForecastItem implements Serializable {
        @SerializedName("dt_txt")
        public String dateTime;

        @SerializedName("main")
        public Main main;

        @SerializedName("weather")
        public List<Weather> weather;
    }

    public static class Main implements Serializable {
        @SerializedName("temp")
        public float temp;

        @SerializedName("temp_min")
        public float tempMin;

        @SerializedName("temp_max")
        public float tempMax;

        @SerializedName("humidity")
        public float humidity;
    }

    public static class Weather implements Serializable {
        @SerializedName("icon")
        public String icon;

        @SerializedName("description")
        public String description;
    }
}
