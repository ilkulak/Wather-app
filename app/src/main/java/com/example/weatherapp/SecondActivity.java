package com.example.weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import android.util.Log;

public class SecondActivity extends AppCompatActivity {

    // Объявление переменных для элементов интерфейса
    private EditText user_field;          // Поле для ввода города
    private Button mainbutton;            // Кнопка поиска погоды
    private TextView result_info;         // Текст для отображения результата
    private Button buttonn;               // Кнопка перехода к ThirdActivity

    // Переменные для хранения данных
    private String city = "";             // Название города
    private String weatherData = "";      // Сырые данные о погоде в JSON формате
    private ArrayList<String> searchHistory;  // История поиска городов
    private ArrayAdapter<String> historyAdapter; // Адаптер для истории
    private SharedPreferences sharedPreferences; // Для хранения истории между сессиями
    private static final String PREFS_NAME = "WeatherAppPrefs"; // Имя файла настроек
    private static final String HISTORY_KEY = "searchHistory";  // Ключ для истории
    private PopupWindow historyPopup;     // Всплывающее окно истории

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Загружаем макет из activity_second.xml
        setContentView(R.layout.activity_second);

        // Инициализация элементов интерфейса по их ID
        user_field = findViewById(R.id.user_field);
        mainbutton = findViewById(R.id.mainbutton);
        result_info = findViewById(R.id.result_info);
        buttonn = findViewById(R.id.buttonn);

        // Инициализация SharedPreferences для сохранения данных
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Инициализация и загрузка истории поиска
        searchHistory = new ArrayList<>();
        loadSearchHistory();

        // Создаём адаптер для отображения истории в ListView
        historyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1); // Простой макет для элементов

        // Настройка всплывающего окна истории
        setupHistoryPopup();

        // Изначально кнопка перехода отключена
        buttonn.setEnabled(false);

        // Обработчик клика по кнопке поиска погоды
        mainbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputCity = user_field.getText().toString().trim(); // Получаем текст из поля
                if (inputCity.equals("")) { // Проверяем, не пустое ли поле
                    // Показываем сообщение об ошибке
                    Toast.makeText(SecondActivity.this, R.string.Usersstr, Toast.LENGTH_LONG).show();
                    buttonn.setEnabled(false);
                } else {
                    // Ищем погоду для указанного города
                    searchWeather(inputCity);

                    // Закрываем popup если открыт
                    if (historyPopup != null && historyPopup.isShowing()) {
                        historyPopup.dismiss();
                    }
                }
            }
        });

        // Обработчик клика по кнопке перехода к детальной информации
        buttonn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!weatherData.isEmpty() && !city.isEmpty()) {
                    // Создаём Intent для перехода к ThirdActivity
                    Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
                    // Передаём данные через Intent
                    intent.putExtra("CITY_NAME", city);
                    intent.putExtra("WEATHER_DATA", weatherData);
                    startActivity(intent);
                } else {
                    Toast.makeText(SecondActivity.this, "Please search for weather first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Показ истории при клике на поле ввода
        user_field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryPopup(v); // Отображаем всплывающее окно с историей
            }
        });

        // Показ истории при получении фокуса полем ввода
        user_field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !searchHistory.isEmpty()) {
                    showHistoryPopup(v); // Показываем историю если есть что показать
                }
            }
        });
    }

    // Метод для настройки всплывающего окна истории
    private void setupHistoryPopup() {
        // Создаём View для popup из макета popup_history.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_history, null);

        // Находим ListView внутри popup
        ListView historyListView = popupView.findViewById(R.id.historyListView);
        historyListView.setAdapter(historyAdapter); // Устанавливаем адаптер

        // Создаём само popup окно
        historyPopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, // Ширина popup
                ViewGroup.LayoutParams.WRAP_CONTENT, // Высота по содержимому
                true // Фокус внутри popup
        );

        // Настройка внешнего вида popup
        historyPopup.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        historyPopup.setElevation(20); // Тень для popup

        // Обработчик клика по элементу истории
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = searchHistory.get(position); // Получаем выбранный город
                user_field.setText(selectedCity); // Вставляем в поле ввода
                searchWeather(selectedCity); // Ищем погоду для этого города
                historyPopup.dismiss(); // Закрываем popup
            }
        });

        // Долгое нажатие для удаления элемента из истории
        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String cityToRemove = searchHistory.get(position);
                removeFromHistory(cityToRemove); // Удаляем из истории
                Toast.makeText(SecondActivity.this, "Removed: " + cityToRemove, Toast.LENGTH_SHORT).show();
                return true; // Обработка события завершена
            }
        });

        // Кнопка очистки всей истории в popup
        Button clearHistoryBtn = popupView.findViewById(R.id.clearHistoryBtn);
        clearHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory(); // Очищаем историю
                historyPopup.dismiss(); // Закрываем popup
            }
        });
    }

    // Метод для отображения popup с историей
    private void showHistoryPopup(View anchorView) {
        if (!searchHistory.isEmpty()) {
            // Обновляем данные в адаптере
            historyAdapter.clear();
            historyAdapter.addAll(searchHistory);
            historyAdapter.notifyDataSetChanged();

            // Показываем popup под указанным View
            historyPopup.showAsDropDown(anchorView, 0, 10);
        }
    }

    // Метод для поиска погоды
    private void searchWeather(String cityName) {
        city = cityName; // Сохраняем город
        String Key = "6751b7bde220531de3e7a1f8d4d004de"; // API ключ OpenWeatherMap
        // Формируем URL запроса
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + Key + "&units=metric";

        // Добавляем город в историю поиска
        addToHistory(cityName);

        // Выполняем запрос к API в отдельном потоке
        new GetURLData().execute(url);
    }

    // Метод для загрузки истории из SharedPreferences
    private void loadSearchHistory() {
        // Загружаем историю как Set<String> из SharedPreferences
        Set<String> historySet = sharedPreferences.getStringSet(HISTORY_KEY, new HashSet<String>());

        // Конвертируем Set в ArrayList для удобства работы
        searchHistory = new ArrayList<>(historySet);

        // Сортируем в обратном порядке (новые запросы сверху)
        Collections.reverse(searchHistory);

        Log.d("History", "Loaded history: " + searchHistory.size() + " items");
    }

    // Метод для сохранения истории в SharedPreferences
    private void saveSearchHistory() {
        try {
            // Создаем новый HashSet из текущей истории
            Set<String> historySet = new HashSet<>(searchHistory);

            // Сохраняем в SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(HISTORY_KEY, historySet);

            // Используем commit() для гарантированного сохранения (немедленного)
            boolean success = editor.commit();

            if (success) {
                Log.d("History", "History saved successfully: " + historySet.size() + " items");
            } else {
                Log.e("History", "Failed to save history");
            }
        } catch (Exception e) {
            Log.e("History", "Error saving history: " + e.getMessage());
        }
    }

    // Метод для добавления города в историю
    private void addToHistory(String cityName) {
        // Форматируем название города (первая буква заглавная, остальные строчные)
        String formattedCity = cityName.substring(0, 1).toUpperCase() +
                cityName.substring(1).toLowerCase();

        // Удаляем если уже существует (предотвращаем дубли)
        for (int i = 0; i < searchHistory.size(); i++) {
            if (searchHistory.get(i).equalsIgnoreCase(cityName)) {
                searchHistory.remove(i);
                break;
            }
        }

        // Добавляем в начало списка (чтобы новые были сверху)
        searchHistory.add(0, formattedCity);

        // Ограничиваем размер истории (максимум 10 записей)
        if (searchHistory.size() > 10) {
            searchHistory = new ArrayList<>(searchHistory.subList(0, 10));
        }

        // Сохраняем изменения в SharedPreferences
        saveSearchHistory();
    }

    // Метод для удаления города из истории
    private void removeFromHistory(String cityName) {
        searchHistory.remove(cityName);
        saveSearchHistory(); // Сохраняем изменения

        // Обновляем адаптер если он существует
        if (historyAdapter != null) {
            historyAdapter.remove(cityName);
            historyAdapter.notifyDataSetChanged();
        }
    }

    // Метод для очистки всей истории
    private void clearHistory() {
        searchHistory.clear();
        saveSearchHistory(); // Сохраняем пустую историю

        // Обновляем адаптер
        if (historyAdapter != null) {
            historyAdapter.clear();
            historyAdapter.notifyDataSetChanged();
        }

        Toast.makeText(this, "Search history cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Закрываем popup при уничтожении активности
        if (historyPopup != null && historyPopup.isShowing()) {
            historyPopup.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Сохраняем историю при уходе из активности (при сворачивании приложения)
        saveSearchHistory();
    }

    // Внутренний класс для выполнения сетевых запросов в фоновом потоке
    private class GetURLData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Действия перед выполнением запроса (в UI потоке)
            result_info.setText("Searching...");
            buttonn.setEnabled(false); // Отключаем кнопку до получения данных
        }

        @Override
        protected String doInBackground(String... strings) {
            // Выполнение в фоновом потоке (не в UI потоке)
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]); // URL из параметра
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000); // Таймаут соединения 10 секунд
                connection.setReadTimeout(10000);    // Таймаут чтения 10 секунд
                connection.connect(); // Устанавливаем соединение

                // Получаем поток данных
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                // Читаем данные построчно
                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString(); // Возвращаем результат

            } catch (MalformedURLException e) {
                Log.e("Weather", "Invalid URL: " + e.getMessage());
            } catch (IOException e) {
                Log.e("Weather", "Network error: " + e.getMessage());
                // Показываем Toast в UI потоке
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SecondActivity.this, "Network error. Please check connection.", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                // Закрываем соединения в блоке finally (гарантированное выполнение)
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null; // Возвращаем null при ошибке
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Действия после выполнения запроса (в UI потоке)

            if (result != null) {
                weatherData = result; // Сохраняем сырые данные
                try {
                    // Парсим JSON ответ
                    JSONObject jsonObject = new JSONObject(result);
                    // Получаем температуру
                    double temp = jsonObject.getJSONObject("main").getDouble("temp");
                    // Получаем описание погоды
                    String description = jsonObject.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("description");

                    // Форматируем и отображаем результат
                    result_info.setText(String.format("%.1f°C, %s in %s", temp, description, city));
                    buttonn.setEnabled(true); // Активируем кнопку перехода

                } catch (JSONException e) {
                    Log.e("Weather", "JSON parsing error: " + e.getMessage());
                    result_info.setText("Error parsing weather data");
                    buttonn.setEnabled(false);
                }
            } else {
                result_info.setText("Failed to get weather data for " + city);
                buttonn.setEnabled(false);
            }
        }
    }
}