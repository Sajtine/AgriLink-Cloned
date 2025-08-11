package com.example.loginappclone;

public class WeatherSuggestion {

    public static String getSuggestion(double humidity, double windSpeed, double temperature, String description) {

        StringBuilder suggestion = new StringBuilder();

        //  High Humidity
        if(humidity > 80){
            suggestion.append("High humidity detected. Risk fungal diseases.\n");
        }

        // High Temperature
        if(temperature > 35){
            suggestion.append("Very hot weather. Ensure crops are well watered.\n");
        }else if(temperature < 10){
            suggestion.append("Cold weather expected. Protect sensitive crops.\n");
        }

        // Rain detection
        if(description.contains("rain")){
         suggestion.append("Rain forecasted. Delay irrigations plans.\n");
        }

        // if no warnings
        if(suggestion.length() ==0){
            suggestion.append("Weather conditions are good for farming activities!");
        }

        return suggestion.toString();
    };
}
