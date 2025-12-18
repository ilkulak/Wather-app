package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class ThirdActivity extends AppCompatActivity {

    // Объявление переменных для элементов интерфейса
    private TextView cityNameTextView;    // Для отображения названия города
    private TextView third_info;          // Для отображения влажности
    private TextView third_info2;         // Для отображения давления
    private TextView third_info3;         // Для отображения скорости ветра
    private Button back_button_third;     // Кнопка возврата назад
    private ImageView weatherIconImageView; // Иконка погоды

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Загружаем макет из activity_third.xml
        setContentView(R.layout.activity_third);

        // Логирование для отладки
        Log.d("ThirdActivity", "onCreate called");

        // Инициализация элементов интерфейса по их ID
        cityNameTextView = findViewById(R.id.city_name_textview);
        weatherIconImageView = findViewById(R.id.weather_icon_imageview);
        third_info = findViewById(R.id.third_info);
        third_info2 = findViewById(R.id.third_info2);
        third_info3 = findViewById(R.id.third_info3);
        back_button_third = findViewById(R.id.back_button_third);

        // Проверка успешной инициализации ImageView (иконки погоды)
        if (weatherIconImageView == null) {
            Log.e("ThirdActivity", "weatherIconImageView is null!");
            Toast.makeText(this, "Error: ImageView not found", Toast.LENGTH_LONG).show();
        } else {
            Log.d("ThirdActivity", "ImageView initialized successfully");
        }

        // Получаем данные, переданные из SecondActivity через Intent
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("ThirdActivity", "Intent is null!");
            return;
        }

        // Извлекаем данные из Intent
        String city = intent.getStringExtra("CITY_NAME");
        String weatherData = intent.getStringExtra("WEATHER_DATA");

        // Логирование полученных данных
        Log.d("ThirdActivity", "City: " + city);
        Log.d("ThirdActivity", "WeatherData length: " +
                (weatherData != null ? weatherData.length() : 0));

        // Устанавливаем название города в TextView
        if (city != null && !city.isEmpty()) {
            cityNameTextView.setText("Weather in " + city);
        } else {
            cityNameTextView.setText("Weather Information"); // Заглушка, если город не указан
        }

        // Парсим и отображаем погодные данные, если они получены
        if (weatherData != null && !weatherData.isEmpty()) {
            try {
                // Преобразуем строку в JSON объект
                JSONObject jsonObject = new JSONObject(weatherData);

                // Получаем объекты main и wind из JSON
                JSONObject main = jsonObject.getJSONObject("main");  // Основные параметры
                JSONObject wind = jsonObject.getJSONObject("wind");  // Данные о ветре

                // Получаем температуру
                double temperature = main.getDouble("temp");

                // Переменные для описания погоды
                String weatherDescription = "";
                String mainWeather = "";

                // Проверяем наличие данных о погоде и извлекаем их
                if (jsonObject.has("weather") && jsonObject.getJSONArray("weather").length() > 0) {
                    JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
                    weatherDescription = weather.getString("description").toLowerCase(); // Описание
                    mainWeather = weather.getString("main").toLowerCase();              // Категория

                    // Логирование погодных данных
                    Log.d("ThirdActivity", "Weather desc: " + weatherDescription);
                    Log.d("ThirdActivity", "Weather main: " + mainWeather);
                }

                // Устанавливаем соответствующую иконку погоды
                setWeatherIcon(temperature, weatherDescription, mainWeather);

                // Извлекаем дополнительные параметры погоды
                int humidity = main.getInt("humidity");           // Влажность в процентах
                double pressure = main.getDouble("pressure");     // Давление в гектопаскалях
                double windSpeed = wind.getDouble("speed");       // Скорость ветра в м/с

                // Отображаем данные в соответствующих TextView
                third_info.setText("Humidity: " + humidity + "%");
                third_info2.setText("Pressure: " + String.format("%.1f", pressure) + " hPa");
                third_info3.setText("Wind Speed: " + String.format("%.1f", windSpeed) + " m/s");

            } catch (JSONException e) {
                // Обработка ошибок парсинга JSON
                Log.e("ThirdActivity", "JSON parsing error: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Error displaying weather data", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Если данные не получены
            Log.e("ThirdActivity", "Weather data is null or empty");
            Toast.makeText(this, "No weather data available", Toast.LENGTH_SHORT).show();
        }

        // Настройка обработчика клика для кнопки возврата
        back_button_third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Закрывает текущую активность и возвращает к предыдущей
            }
        });
    }

    // Метод для выбора и установки иконки погоды в зависимости от условий
    private void setWeatherIcon(double temperature, String description, String mainWeather) {
        int iconResource; // ID ресурса иконки

        // Логирование параметров для отладки
        Log.d("WeatherIcon", "Setting icon - Temp: " + temperature +
                ", Desc: " + description + ", Main: " + mainWeather);

        // Определяем, какую иконку использовать на основе погодных условий
        if (mainWeather.contains("clear")) {
            // Ясная погода
            iconResource = R.drawable.ic_sunny;
            Log.d("WeatherIcon", "Using sunny icon");
        } else if (mainWeather.contains("rain") || description.contains("rain")) {
            // Дождь
            iconResource = R.drawable.ic_rainy;
            Log.d("WeatherIcon", "Using rainy icon");
        } else if (mainWeather.contains("snow") || description.contains("snow")) {
            // Снег
            iconResource = R.drawable.ic_snowy;
            Log.d("WeatherIcon", "Using snowy icon");
        } else if (mainWeather.contains("cloud")) {
            // Облачно
            iconResource = R.drawable.ic_cloudy;
            Log.d("WeatherIcon", "Using cloudy icon");
        } else if (temperature > 25) {
            // Жарко (более 25°C)
            iconResource = R.drawable.ic_sunny;
            Log.d("WeatherIcon", "Using sunny icon (hot)");
        } else if (temperature < 5) {
            // Холодно (менее 5°C)
            iconResource = R.drawable.ic_snowy;
            Log.d("WeatherIcon", "Using snowy icon (cold)");
        } else {
            // По умолчанию
            iconResource = R.drawable.weather_svgrepo_com;
            Log.d("WeatherIcon", "Using default icon");
        }

        // Устанавливаем иконку в ImageView с обработкой ошибок
        try {
            weatherIconImageView.setImageResource(iconResource);
            Log.d("WeatherIcon", "Icon set successfully: " + iconResource);
        } catch (Exception e) {
            // Ошибка при установке иконки (например, ресурс не найден)
            Log.e("WeatherIcon", "Error setting image: " + e.getMessage());

            // Попробуем использовать системную иконку в качестве запасного варианта
            try {
                weatherIconImageView.setImageResource(android.R.drawable.ic_dialog_info);
            } catch (Exception e2) {
                Log.e("WeatherIcon", "Failed to set fallback icon");
            }
        }
    }
}