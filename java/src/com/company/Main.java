package com.company;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import org.json.*; // can be downloaded from https://github.com/stleary/JSON-java

public class Main {

    public static void main(String[] args) {
        try {
            String token = login(Constants.email, Constants.password);

            // Get list of projects
            JSONArray projects = getQualityProjects(token);
            for (int i = 0; i < projects.length(); i++) {
                JSONObject project = projects.getJSONObject(i);
                System.out.println(project.getString("name"));
            }

            // Get list of new files since last backup date
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date yesterday = cal.getTime();
            JSONArray files = getNewFilesSince(token, yesterday);
            // Download each file one by one
            int len = files.length();
            for (int i = 0; i < len; i++) {
                System.out.println("Download file " + (i+1) + "/" + len);
                JSONObject file = files.getJSONObject(i);
                try {
                    downloadFile(Constants.apiUrl, token, file.getString("id"));
                } catch(Exception e) {
                    System.out.println("Something went wrong");
                    // Maybe retry
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String login(String email, String password) throws Exception {
        String credentials = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        String response = get("/token", "Basic " + credentials);

        if (response.length() != 0) {
            JSONObject obj = new JSONObject(response);
            return obj.getString("token");
        } else {
            throw new Exception("Login error : bad email / password");
        }
    }

    private static JSONArray getQualityProjects(String token) throws Exception {
        String response = get("/quality/projects", "Bearer " + token);
        return new JSONArray(response);
    }

    private static JSONArray getNewFilesSince(String token, Date date) throws Exception {
        String response = get("/files/since/" + date.getTime(), "Bearer " + token);
        return new JSONArray(response);
    }

    private static void downloadFile(String url, String token, String id) throws Exception {
        URL urlObj = new URL(url + "/files/download/" + id);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        // Get response
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Error: " + responseCode);
        }

        InputStream is = connection.getInputStream();
        String outputFile = "downloads\\" + id;
        Files.copy(is, Paths.get(outputFile));
        is.close();
    }

    private static String get(String url, String authorization) throws Exception {
        URL urlObj = new URL(Constants.apiUrl + url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authorization);

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

        return response.toString();
    }
}
