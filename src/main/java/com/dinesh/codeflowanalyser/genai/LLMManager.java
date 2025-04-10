package com.dinesh.codeflowanalyser.genai;

/* START GENAI@COPILOT */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.dinesh.codeflowanalyser.genai.GenAiApiClient.*;

public class LLMManager {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String accessToken = getAccessToken(username, password);
        if(!isAccessTokenValid(accessToken)){
            System.out.println("Invalid Access Token");
            return;
        }

        String appLocation = getApplication(accessToken);
        String[] models = getSupportedModels(accessToken);
        System.out.println(queryLLM(appLocation, models[2], accessToken ));
    }

    public static String invokeLLM(List<String> code){
        String out = queryLLM_hackathon("gpt-4o", code);
        String ret = "";
        try{
            ret = extractAndPrettyPrint_ModelPreview(out);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return ret;
    }

    public static void invokeLLM(List<String> code, String className, String method){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = "";

        System.out.print("Enter password: ");
        String password = "";

        String accessToken = getAccessToken(username, password);
        if(!isAccessTokenValid(accessToken)){
            System.out.println("Invalid Access Token");
            return;
        }

        String application = getApplication(accessToken);
        //String[] models = getSupportedModels(accessToken);
        String out = queryLLM_hackathon(application, "gpt-4o-2", accessToken, Collections.singletonList(code), className, method);
        try {
            extractAndPrettyPrint(out);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String extractAndPrettyPrint(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        StringBuilder sb = new StringBuilder();

        // Extract message content
        String messageContent = rootNode.path("full_model_response").path("choices").path(0).path("message").path("content").asText();
        sb.append("Message Content");

        System.out.println("Message Content");
        System.out.println(messageContent);

        // Extract usage
        JsonNode usageNode = rootNode.path("full_model_response").path("usage");
        String prettyPrintedStr = prettyPrintJson(usageNode.toString());
        sb.append("Usage:");
        sb.append(prettyPrintedStr);

        System.out.println("Usage:");
        System.out.println(prettyPrintedStr);
        return sb.toString();
    }

    public static String prettyPrintJson(String jsonString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Object json = objectMapper.readValue(jsonString, Object.class);
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        return writer.writeValueAsString(json);
    }

    public static String extractAndPrettyPrint_ModelPreview(String jsonResponse) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        StringBuilder sb = new StringBuilder();

        // Extract message content
        String messageContent = rootNode.path("choices").path(0).path("message").path("content").asText();
        sb.append("Message Content");
        sb.append(messageContent);

        System.out.println("Message Content");
        System.out.println(messageContent);

        // Extract usage
        JsonNode usageNode = rootNode.path("usage");
        String usageStr = prettyPrintJson(usageNode.toString());
        sb.append("Usage:");
        sb.append(usageStr);

        System.out.println("Usage:");
        System.out.println(usageStr);
        return sb.toString();
    }
}
/* END GENAI@COPILOT */

