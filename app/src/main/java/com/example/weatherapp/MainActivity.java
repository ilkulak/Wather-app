
package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    // Объявление переменной для кнопки
    private Button button_to_second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Загружаем макет из activity_main.xml
        setContentView(R.layout.activity_main);

        // Находим кнопку по её ID в макете
        button_to_second = findViewById(R.id.button_to_second);

        // Устанавливаем обработчик клика на кнопку
        button_to_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаём Intent для перехода на SecondActivity
                // MainActivity.this - текущий контекст
                // SecondActivity.class - целевая активность
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);

                // Запускаем SecondActivity
                startActivity(intent);
            }
        });
    }
}



