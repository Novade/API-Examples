package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.*; // can be downloaded from https://github.com/stleary/JSON-java

public class Main {

    public static void main(String[] args) {
        try {
            String token = login(Constants.email, Constants.password, Constants.apiUrl);

            // Get list of projects
            JSONArray projects = getQualityProjects(Constants.apiUrl, token);
            for (int i = 0; i < projects.length(); i++) {
                JSONObject project = projects.getJSONObject(i);
                System.out.println(project.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String login(String email, String password, String url) throws Exception {
        URL urlObj = new URL(url + "/token");
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        String credentials = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        connection.setRequestProperty("Authorization", "Basic " + credentials);

        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Error: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseStr = response.toString();
        if (responseStr.length() != 0) {
            JSONObject obj = new JSONObject(responseStr);
            return obj.getString("token");
        } else {
            throw new Exception("Login error : bad email / password");
        }
    }

    private static JSONArray getQualityProjects(String url, String token) throws Exception {
        URL urlObj = new URL(url + "/quality/projects");
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Error: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseStr = response.toString();
        return new JSONArray(responseStr);
    }
}