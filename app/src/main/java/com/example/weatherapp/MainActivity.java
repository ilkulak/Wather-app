package com.example.weatherapp;



import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button mainbutton;
    private TextView result_info;
    private TextView result_info2;
    private TextView result_info3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_field = findViewById(R.id.user_field);
        mainbutton = findViewById(R.id.mainbutton);
        result_info = findViewById(R.id.result_info);
        result_info2 = findViewById(R.id.result_info2);
        result_info3 = findViewById(R.id.result_info3);

        mainbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_field.getText().toString().trim().equals(""))
                    Toast.makeText(MainActivity.this, R.string.Usersstr, Toast.LENGTH_LONG).show();
                else {
                    String city = user_field.getText().toString();
                    String Key = "6751b7bde220531de3e7a1f8d4d004de";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + Key + "&units=metric";

                    new GetURLData().execute(url);


                }
            }
        });
    }

    private class GetURLData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Ожидайте...");
        }
        protected void onPreExecute2() {
            super.onPreExecute();
            result_info2.setText("Ожидайте...");
        }
        protected void onPreExecute3() {
            super.onPreExecute();
            result_info3.setText("Ожидайте...");
        }


        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                    } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                result_info.setText("Temperature = "+jsonObject.getJSONObject("main").getDouble("temp"));
                result_info2.setText("Wind = "+jsonObject.getJSONObject("wind").getDouble("speed"));
                result_info3.setText("Cloud cover = "+jsonObject.getJSONObject("clouds").getDouble("all"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}


