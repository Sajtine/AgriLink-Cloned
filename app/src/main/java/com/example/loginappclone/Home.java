package com.example.loginappclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity {

    private ImageView loc, user;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    ForecastResponse forecastResponse;
    WeatherResponse currentWeather;


    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int backgroundColor = ContextCompat.getColor(this, R.color.white);
        if (isColorDark(backgroundColor)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        loc = findViewById(R.id.location);
        user = findViewById(R.id.user_profile);

        loc.setOnClickListener(v -> startActivity(new Intent(Home.this, Market_Location.class)));
        user.setOnClickListener(v -> startActivity(new Intent(Home.this, Profile.class)));

        addFragment();

        // Navigation to Weather Reports Page
        TextView weather_report = findViewById(R.id.weatherReport);
        weather_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(forecastResponse != null){
                   Intent intent = new Intent(Home.this, WeatherReport.class);
                   intent.putExtra("forecastData", forecastResponse);
                   intent.putExtra("currentWeatherData", currentWeather);
                   startActivity(intent);
               }else{
                   Toast.makeText(Home.this, "No weather data available", Toast.LENGTH_SHORT).show();
               }
            }
        });

        // Location initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        // Display crops suggestion
        displayCrops();
    }

    private void popupWindow(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.calendar_popup, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, -450);
    }

    public void addFragment() {
        Fragment fragment = new topNav();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.top_nav_container, fragment);
        ft.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 🔄 Reload the topNav fragment to refresh the badge or icon
        topNav topFragment = (topNav) getSupportFragmentManager().findFragmentById(R.id.top_nav_container); // use your actual container ID
        if (topFragment != null) {
            topFragment.refreshBadge(); // Custom method you’ll add below
        }

        ImageView home = findViewById(R.id.home);
        home.setImageResource(R.drawable.home1);
        Log.d("Activity Checker", "Home");
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getWeatherData(latitude, longitude);
                    } else {
                        // fallback if location is unavailable
                        getWeatherData(10.7202, 122.5621);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Showing default weather.", Toast.LENGTH_SHORT).show();
                getWeatherData(10.7202, 122.5621);
            }
        }
    }

    // Get current weather data
    public void getWeatherData(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        // Step 1: First get current weather
        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeatherByCoordinates(lat, lon, "2def0fc1c9ce3fc0cffa4d2c2adca8b6", "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentWeather = response.body();

                    // Step 2: Then get forecast
                    getForecastData(lat, lon, currentWeather);
                } else {
                    Toast.makeText(Home.this, "Failed to load current weather.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(Home.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // get forecast data
    public void getForecastData(double lat, double lon, WeatherResponse currentWeather){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        Call<ForecastResponse> forecastCall = weatherApi.getForecastByCoordinates(lat, lon, "2def0fc1c9ce3fc0cffa4d2c2adca8b6", "metric");

        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    ForecastResponse forecastResponse = response.body();
                    Home.this.forecastResponse = response.body();

                    displayWeather(currentWeather, forecastResponse);

                }else{
                    Toast.makeText(Home.this, "Failed to load forecast.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Toast.makeText(Home.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Display the weather
    private void displayWeather(WeatherResponse currentWeather, ForecastResponse forecastResponse) {
        LinearLayout weatherContainer = findViewById(R.id.weatherContainer);
        weatherContainer.removeAllViews();

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat onlyDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);

        Date currentDate = new Date();
        String todayDateStr = onlyDateFormat.format(currentDate);

        Map<String, List<ForecastResponse.ForecastItem>> dailyForecasts = new HashMap<>();

        for (ForecastResponse.ForecastItem item : forecastResponse.list) {
            try {
                Date forecastDateTime = fullDateFormat.parse(item.dateTime);
                String forecastDateStr = onlyDateFormat.format(forecastDateTime);

                dailyForecasts.computeIfAbsent(forecastDateStr, k -> new ArrayList<>()).add(item);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        List<String> sortedDates = new ArrayList<>(dailyForecasts.keySet());
        Collections.sort(sortedDates);

        // 1. Today (use currentWeather)
        View todayCard = LayoutInflater.from(Home.this).inflate(R.layout.weather_card_item, weatherContainer, false);

        ImageView weatherIcon = todayCard.findViewById(R.id.weatherIcon);
        TextView dayText = todayCard.findViewById(R.id.dayText);
        TextView tempText = todayCard.findViewById(R.id.tempText);
        TextView minMax = todayCard.findViewById(R.id.minMax);

        String iconCode = currentWeather.weather.get(0).icon;
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(Home.this).load(iconUrl).into(weatherIcon);

        dayText.setText("Today");
        tempText.setText(Math.round(currentWeather.main.temp) + "°C");

        String minMax_str = Math.round(currentWeather.main.temp_min) + "° / " + Math.round(currentWeather.main.temp_max) + "°";
        minMax.setText(minMax_str);

        weatherContainer.addView(todayCard);

        // 2. Next Days (use forecast)
        for (String forecastDateStr : sortedDates) {
            if (forecastDateStr.equals(todayDateStr)) {
                continue; // Skip today since we already showed it
            }

            List<ForecastResponse.ForecastItem> dayItems = dailyForecasts.get(forecastDateStr);

            ForecastResponse.ForecastItem bestItem = null;
            long minDifference = Long.MAX_VALUE;
            float minTemp = Float.MAX_VALUE;
            float maxTemp = Float.MIN_VALUE;

            for (ForecastResponse.ForecastItem item : dayItems) {
                try {
                    Date forecastDateTime = fullDateFormat.parse(item.dateTime);

                    Calendar targetCalendar = Calendar.getInstance();
                    targetCalendar.setTime(forecastDateTime);
                    targetCalendar.set(Calendar.HOUR_OF_DAY, 12);
                    targetCalendar.set(Calendar.MINUTE, 0);
                    targetCalendar.set(Calendar.SECOND, 0);

                    long difference = Math.abs(forecastDateTime.getTime() - targetCalendar.getTimeInMillis());
                    if (difference < minDifference) {
                        minDifference = difference;
                        bestItem = item;
                    }

                    if (item.main.tempMin < minTemp) minTemp = item.main.tempMin;
                    if (item.main.tempMax > maxTemp) maxTemp = item.main.tempMax;

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (bestItem != null) {
                try {
                    Date bestForecastDate = fullDateFormat.parse(bestItem.dateTime);

                    String dayLabel = dayNameFormat.format(bestForecastDate);

                    View card = LayoutInflater.from(Home.this).inflate(R.layout.weather_card_item, weatherContainer, false);

                    ImageView forecastWeatherIcon = card.findViewById(R.id.weatherIcon);
                    TextView forecastDayText = card.findViewById(R.id.dayText);
                    TextView forecastTempText = card.findViewById(R.id.tempText);
                    TextView forecastMinMax = card.findViewById(R.id.minMax);

                    String forecastIconCode = bestItem.weather.get(0).icon;
                    String forecastIconUrl = "https://openweathermap.org/img/wn/" + forecastIconCode + "@2x.png";
                    Glide.with(Home.this).load(forecastIconUrl).into(forecastWeatherIcon);

                    forecastDayText.setText(dayLabel);
                    forecastTempText.setText(Math.round(bestItem.main.temp) + "°C");
                    forecastMinMax.setText(Math.round(minTemp) + "° / " + Math.round(maxTemp) + "°");

                    weatherContainer.addView(card);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Display crops
    private void displayCrops() {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        Cursor cursor = dbHelper.getCropsForMonth(currentMonth);

        if (cursor.moveToFirst()) {
            GridLayout cropContainer = findViewById(R.id.cropContainer);
            cropContainer.removeAllViews();
            cropContainer.setColumnCount(2);

            LayoutInflater inflater = LayoutInflater.from(this);

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int margin = 32; // 16dp margin on each side (you set 16 above)
            int cardWidth = (screenWidth - margin * 3) / 2; // 3 margins: left + right + middle space

            do {
                String crop_name = cursor.getString(cursor.getColumnIndexOrThrow("crop_name"));
                Log.d("Crop_Debug", "Crop Name: " + crop_name );

                View cardView = inflater.inflate(R.layout.crop_card, cropContainer, false);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cardWidth;
                params.setMargins(margin / 2, margin / 2, margin / 2, margin / 2);
                cardView.setLayoutParams(params);

                // Set crop name
                TextView cropName = cardView.findViewById(R.id.cropName);
                cropName.setText(crop_name);

                ImageView cropImage = cardView.findViewById(R.id.cropImage);

                // to determine what crop image to display
                crop_name = crop_name.toLowerCase();

                switch(crop_name){
                    case "okra":
                        cropImage.setImageResource(R.drawable.okra);
                        break;
                    case "banana":
                        cropImage.setImageResource(R.drawable.banana);
                        break;
                    case "coconut":
                        cropImage.setImageResource(R.drawable.coconut);
                        break;
                    case "watermelon":
                        cropImage.setImageResource(R.drawable.watermelon);
                        break;
                    case "papaya":
                        cropImage.setImageResource(R.drawable.papaya);
                        break;
                    case "pineapple":
                        cropImage.setImageResource(R.drawable.pineapple);
                        break;
                    case "calamansi":
                        cropImage.setImageResource(R.drawable.calamnsi);
                        break;
                    case "melon":
                        cropImage.setImageResource(R.drawable.melon);
                        break;
                    case "corn":
                        cropImage.setImageResource(R.drawable.corn);
                        break;
                    case "peanut":
                        cropImage.setImageResource(R.drawable.peanut);
                        break;
                    case "kamote":
                        cropImage.setImageResource(R.drawable.kamote);
                        break;
                    case "rice":
                        cropImage.setImageResource(R.drawable.rice);
                        break;
                    case "cassava":
                        cropImage.setImageResource(R.drawable.cassava);
                        break;
                    case "chayote":
                        cropImage.setImageResource(R.drawable.chayote);
                        break;
                    case "eggplant":
                        cropImage.setImageResource(R.drawable.eggplant);
                        break;
                    case "ampalaya":
                        cropImage.setImageResource(R.drawable.ampalaya);
                        break;
                    default:
                        cropImage.setImageResource(R.drawable.temp);
                        break;
                }


                cropContainer.addView(cardView);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }



}
