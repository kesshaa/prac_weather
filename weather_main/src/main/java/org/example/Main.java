package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final String API_KEY = "5f689ae81f863b06761dac2445db5f12";
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Введите город (или 'Выход' для завершения): ");
            String city = scanner.nextLine();

            if (city.equalsIgnoreCase("Выход")) {
                break;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";
                    URL url = new URL(urlString);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 404) {
                        System.out.println("ERROR 404 \nГород не найден. Пожалуйста, попробуйте еще раз.");
                        return;
                    } else if (responseCode == 400) {
                        System.out.println("ERROR 400 \nНеверный запрос. Проверьте введенные данные.");
                        return;
                    } else if (responseCode == 401) {
                        System.out.println("ERROR 401 \nНеверный API-ключ.");
                        return;
                    } else if (responseCode == 500) {
                        System.out.println("ERROR 500 \nОшибка на сервере. Попробуйте позже.");
                        return;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject main = jsonResponse.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    int humidity = main.getInt("humidity");
                    double windSpeed = jsonResponse.getJSONObject("wind").getDouble("speed");
                    String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");

                    System.out.println("Погода в " + city + ":");
                    System.out.println("Температура: " + temperature + "°C");
                    System.out.println("Влажность: " + humidity + "%");
                    System.out.println("Скорость ветра: " + windSpeed + " м/с");
                    System.out.println("Описание: " + weatherDescription);
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            });
            future.get();
        }
        scanner.close();
    }
}
