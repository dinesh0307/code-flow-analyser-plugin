package com.dinesh.codeflowanalyser.genai;


import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static com.dinesh.codeflowanalyser.genai.TokenHelper.*;

public class GenAiApiClient {
    static {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };


        try {
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            //Create all-trsuting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            //Install the all-trusting host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken(String username, String password) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/auth/login";
        String data = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
        return extractAccessToken(Objects.requireNonNull(sendPostRequest(urlString, data, null)));
    }

    public static boolean isAccessTokenValid(String accessToken) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/auth/validate";
        return sendGetRequest(urlString, accessToken) != null;
    }

    public static String getApplication(String accessToken) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/applications/";
        return extractApplicationName(Objects.requireNonNull(sendGetRequest(urlString, accessToken)));
    }

    public static String[] getSupportedModels(String accessToken) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/models/";
        return parseModelNames(Objects.requireNonNull(sendGetRequest(urlString, accessToken)));
    }

    public static String queryLLM(String applicationName, String modelName, String accessToken) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/queries/chat";
        JSONObject data = new JSONObject();
        data.put("model_name", modelName);
        data.put("application_name", applicationName);
        JSONArray query = new JSONArray();
        query.put(new JSONObject().put("role", "system").put("content", "You are a AI chat bot"));
        query.put(new JSONObject().put("role", "user").put("content", "say something about US constitution"));
        data.put("query", query);
        return sendPostRequest(urlString, data.toString(), accessToken);
    }

    public static String queryLLM_hackathon(String applicationName, String modelName, String accessToken, List<String> code, String className, String method) {
        String urlString = "https://genai-api.visa.com/genai-api/v1/queries/chat";
        JSONObject data = new JSONObject();
        data.put("model_name", modelName);
        data.put("application_name", applicationName);
        JSONArray query = new JSONArray();
        query.put(new JSONObject().put("role", "system").put("content", "You are an experienced java developer with good understanding about high level design and low level design and good at understanding the functionality by reading the code"));
        for (String content : code) {
            query.put(new JSONObject().put("role", "user").put("content", content));
        }
        data.put("query", query);
        return sendPostRequest(urlString, data.toString(), accessToken);
    }

    public static String queryLLM_hackathon(String modelName, List<String> code) {
        String urlString = "https://model-service-preview-hackathon.genai.visa.com/v1/chat/completions";

        JSONObject data = new JSONObject();
        data.put("model", modelName);

        JSONArray query = new JSONArray();
        query.put(new JSONObject().put("role", "system").put("content", "You are an experienced java developer with good understanding about high level design and low level design and good at understanding the functionality by reading the code"));
        for (String content : code) {
            query.put(new JSONObject().put("role", "user").put("content", content));
        }
        data.put("messages", query);
        return sendPostRequest(urlString, data.toString());
    }

    private static String sendPostRequest(String urlString, String data){
        try{
            HttpURLConnection connection = getHttpURLConnection(urlString, data);

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                try(Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)){
                    return scanner.useDelimiter("\\A").next();
                }
            }else {
                System.out.println("POST request failed, response code: " + responseCode);
                System.out.println("Failed message :" + connection.getResponseMessage());
                StringBuilder builder = new StringBuilder();

                try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                    String line;
                    while ((line = reader.readLine()) != null){
                        builder.append(line + "\\n");
                    }
                }
                return builder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection getHttpURLConnection(String urlString, String data) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("USERNAME", "");
        connection.setRequestProperty("PASSWORD", "");
        connection.setDoOutput(true);

        try(OutputStream os = connection.getOutputStream()){
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    private static String sendPostRequest(String urlString, String data, String accessToken){
        try{
            HttpURLConnection connection = getHttpURLConnection(urlString, data, accessToken);

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                try(Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)){
                    return scanner.useDelimiter("\\A").next();
                }
            }else {
                System.out.println("POST request failed, response code: " + responseCode);
                System.out.println("Failed message :" + connection.getResponseMessage());
                StringBuilder builder = new StringBuilder();

                try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))){
                    String line;
                    while ((line = reader.readLine()) != null){
                        builder.append(line + "\\n");
                    }
                }
                return builder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection getHttpURLConnection(String urlString, String data, String accessToken) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        if(accessToken != null){
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        try(OutputStream os = connection.getOutputStream()){
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

    private static String sendGetRequest(String urlString, String accessToken){
        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                try(Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)){
                    return scanner.useDelimiter("\\A").next();
                }
            }else {
                System.out.println("GET request failed, response code: " + responseCode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
