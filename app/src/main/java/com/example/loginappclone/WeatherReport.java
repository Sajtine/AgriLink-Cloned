package com.example.loginappclone;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeatherReport extends AppCompatActivity {

    ForecastResponse forecastResponse;
    private List<String> fullDateTimeLabels; // Keep full datetime for day label updates
    private List<String> displayLabels;      // Separate list for showing hours on XAxis
    private boolean clicked = false;
    TextView currentlySelectedTextView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_report);

        // Retrieve passed data
        forecastResponse = (ForecastResponse) getIntent().getSerializableExtra("forecastData");
        WeatherResponse currentWeather = (WeatherResponse) getIntent().getSerializableExtra("currentWeatherData");

        if (currentWeather != null) {
            double temp2 = currentWeather.main.temp;
            double humidity = currentWeather.main.humidity;
            double windSpeedInMS = currentWeather.wind.speed;
            double windSpeedInKMH = windSpeedInMS * 3.6;
            double windDeg = currentWeather.wind.deg;
            String windDirection = getWindDirection(windDeg);
            String weatherCondition = currentWeather.weather.get(0).description;

            ImageView weatherIcon = findViewById(R.id.weather_icon);
            setWeatherIcon(weatherCondition, weatherIcon);

            TextView temp = findViewById(R.id.temp);
            TextView humidityValue = findViewById(R.id.humidity_value);
            TextView windSpeed = findViewById(R.id.wind_value);
            TextView weatherDesc = findViewById(R.id.weather_desc);

            int currentTemp = Math.round((float) temp2);
            int currentHumidity = Math.round((float) humidity);
            int currentWindSpeed = Math.round((float) windSpeedInKMH);

            temp.setText(currentTemp + "°C");
            humidityValue.setText(currentHumidity + "%");
            weatherDesc.setText(weatherCondition);
            windSpeed.setText(currentWindSpeed + "km/h " + windDirection);

            String suggestions = WeatherSuggestion.getSuggestion(
                    currentHumidity, currentWindSpeed, currentTemp, weatherCondition
            );
            TextView farmTip = findViewById(R.id.farm_tip);
            farmTip.setText("🌱 Today's Tip: " + suggestions);
        }

        // Line charts
        List<ForecastResponse.ForecastItem> todayForecast = new ArrayList<>();
        List<ForecastResponse.ForecastItem> futureForecast = new ArrayList<>();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (ForecastResponse.ForecastItem item : forecastResponse.list) {
            if (item.dateTime.startsWith(todayDate)) {
                todayForecast.add(item);
            } else {
                futureForecast.add(item);
            }
        }

        setTodayChart(todayForecast, findViewById(R.id.todays_chart), "Today's Temperature");
        setFutureForecastChart(futureForecast, findViewById(R.id.future_chart), "Upcoming Days Temperature");

        TextView dayLabel = findViewById(R.id.day_label);
        LineChart future_chart = findViewById(R.id.future_chart);

        future_chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                updateDayLabel(future_chart, fullDateTimeLabels, dayLabel);
            }
        });

        Log.d("JSON_Response Weather", new Gson().toJson(forecastResponse));


        // Set per day forecast
        LinearLayout dayContainer = findViewById(R.id.days_container);
        TextView forecastDetails = findViewById(R.id.forecast_details);

        Map<String, List<ForecastResponse.ForecastItem>> forecastByDate = new LinkedHashMap<>();
        for(ForecastResponse.ForecastItem item : forecastResponse.list){
            String date = item.dateTime.substring(0, 10);
            forecastByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String tomorrow = getTomorrowDate(today);

        // Create textview for each day
        for(Map.Entry<String, List<ForecastResponse.ForecastItem>> entry : forecastByDate.entrySet()){
            String dateKey = entry.getKey();

            // skip today date
            if(dateKey.equals(today)){
                continue;
            }

            List<ForecastResponse.ForecastItem> items = entry.getValue();

            String label;
            if(dateKey.equals(tomorrow)){
                label = "Tomorrow";
            }else{
                Date parsed = null;
                try {
                    parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                label = new SimpleDateFormat("EEEE", Locale.getDefault()).format(parsed);

            }

            // Create the TextView
            TextView dayTextView = new TextView(this);
            dayTextView.setText(label);
            dayTextView.setTextSize(16);
            dayTextView.setPadding(40, 25, 40, 25);
            dayTextView.setTextColor(Color.BLACK);
            dayTextView.setClickable(true);

            // Optional layout styling
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(15, 0, 15, 0);
            dayTextView.setLayoutParams(params);

            // Handle clicks
            dayTextView.setOnClickListener(v -> {

                // Reset previous selection
                if (currentlySelectedTextView != null) {
                    currentlySelectedTextView.setBackground(null);
                    currentlySelectedTextView.setTextColor(Color.BLACK);
                }

                // Mark this as selected
                dayTextView.setBackgroundColor(Color.parseColor("#357ABD"));
                dayTextView.setTextColor(Color.WHITE);

                // Update the reference
                currentlySelectedTextView = dayTextView;

                // Forecast Data per Day
                for (ForecastResponse.ForecastItem item : items) {
                    if (item.dateTime.contains("12:00:00")) {
                        int temp = Math.round(item.main.temp);
                        int humidity = Math.round(item.main.humidity);
                        String condition = item.weather.get(0).description;

                        String suggestion = WeatherSuggestion.getSuggestion(humidity, 0, temp, condition);

                        CardView forecastCard = findViewById(R.id.forecastCard);
                        TextView forecastTitle = findViewById(R.id.forecastTitle);
                        TextView tempText = findViewById(R.id.tempText);
                        TextView humidityText = findViewById(R.id.humidityText);
                        TextView conditionText = findViewById(R.id.conditionText);
                        TextView suggestionText = findViewById(R.id.suggestionText);

                        forecastTitle.setText("🌤 12 PM Forecast");
                        tempText.setText("Temp: " + temp + "°C");
                        humidityText.setText("Humidity: " + humidity + "%");
                        conditionText.setText("Condition: " + condition);
                        suggestionText.setText("🌱 Tip for Farmers: " + suggestion);

                        forecastCard.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            });

            dayContainer.addView(dayTextView);
        }
    }

    // Wind direction
    private String getWindDirection(double degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) ((degrees + 22.5) / 45) % 8;
        return directions[index];
    }

    // Set weather icon
    private void setWeatherIcon(String weatherCondition, ImageView weatherIcon) {
        if (weatherCondition.contains("rain")) {
            weatherIcon.setImageResource(R.drawable.rainy);
        } else if (weatherCondition.contains("clouds")) {
            weatherIcon.setImageResource(R.drawable.cloudy);
        } else if (weatherCondition.contains("clear")) {
            weatherIcon.setImageResource(R.drawable.sun);
        } else if (weatherCondition.contains("stormy")) {
            weatherIcon.setImageResource(R.drawable.stormy);
        } else {
            weatherIcon.setImageResource(R.drawable.sun);
        }
    }

    // Current weather
    private void setTodayChart(List<ForecastResponse.ForecastItem> forecastData, LineChart chart, String labelName) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (ForecastResponse.ForecastItem item : forecastData) {
            float temp = item.main.temp;
            entries.add(new Entry(entries.size(), temp));
            labels.add(formatHour(item.dateTime));  // show "3PM", "6PM", etc
        }

        LineDataSet lineDataSet = new LineDataSet(entries, labelName);
        styleLineDataSet(lineDataSet);

        LineData lineData = new LineData(lineDataSet);
        chart.setData(lineData);

        // Format point values (on data points)
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format("%.2f", entry.getY()); // Formats to 2 decimal numbers
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setXOffset(15f);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setLabelRotationAngle(0f);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);

        chart.invalidate();
    }

    // next day forecast
    private void setFutureForecastChart(List<ForecastResponse.ForecastItem> futureForecast, LineChart chart, String labelName) {
        ArrayList<Entry> entries = new ArrayList<>();
        fullDateTimeLabels = new ArrayList<>();
        displayLabels = new ArrayList<>();

        // Use LinkedHashMap to preserve day order
        Map<String, List<ForecastResponse.ForecastItem>> forecastByDay = new LinkedHashMap<>();
        for (ForecastResponse.ForecastItem item : futureForecast) {
            if (item.dateTime != null && item.dateTime.length() >= 10) {
                String day = item.dateTime.substring(0, 10);
                forecastByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(item);
            }
        }

        List<ForecastResponse.ForecastItem> allFutureData = new ArrayList<>();
        for (String day : forecastByDay.keySet()) {
            allFutureData.addAll(forecastByDay.get(day));
        }

        for (ForecastResponse.ForecastItem item : allFutureData) {
            float temp = item.main.temp;
            entries.add(new Entry(entries.size(), temp));
            fullDateTimeLabels.add(item.dateTime);
            displayLabels.add(formatHour(item.dateTime));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, labelName);
        styleLineDataSet(lineDataSet); // You should define this method

        LineData lineData = new LineData(lineDataSet);
        chart.setData(lineData);

        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setVisibleXRangeMaximum(6);
        chart.moveViewToX(0);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(displayLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setXOffset(15f);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);

        chart.invalidate();
    }


    // update label for the line chart
    private void updateDayLabel(LineChart chart, List<String> labels, TextView dayLabel) {
        float centerX = (chart.getLowestVisibleX() + chart.getHighestVisibleX()) / 2;
        int index = Math.round(centerX);

        if (index >= 0 && index < labels.size()) {
            String fullDateTime = labels.get(index);
            String date = fullDateTime.substring(0, 10);

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String tomorrow = getTomorrowDate(today);

            if (date.equals(today)) {
                dayLabel.setText("Today");
            } else if (date.equals(tomorrow)) {
                dayLabel.setText("Forecast: Tomorrow");
            } else {
                try {
                    Date parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                    String weekday = new SimpleDateFormat("EEEE", Locale.getDefault()).format(parsedDate);
                    dayLabel.setText("Forecast: " + weekday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getTomorrowDate(String today) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(today);
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private String formatHour(String dtTxt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dtTxt);
            return new SimpleDateFormat("ha", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void styleLineDataSet(LineDataSet lineDataSet) {
        lineDataSet.setColor(Color.parseColor("#4A90E2"));
        lineDataSet.setCircleColor(Color.parseColor("#4A90E2"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

}
