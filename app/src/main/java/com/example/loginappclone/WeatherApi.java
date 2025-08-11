package com.example.loginappclone;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {

    // Forecast by coordinates
    @GET("forecast")
    Call<ForecastResponse> getForecastByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // forecast by city name
    @GET("forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Current Weather
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

}
