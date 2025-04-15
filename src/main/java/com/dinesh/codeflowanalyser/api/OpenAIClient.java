package com.dinesh.codeflowanalyser.api;

import com.dinesh.codeflowanalyser.dto.Credential;
import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.dinesh.codeflowanalyser.genai.GenAiApiClient;
import com.dinesh.codeflowanalyser.genai.LLMManager;
import com.dinesh.codeflowanalyser.util.ApiKeyManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenAIClient implements ApiClient {
    //private static final String MODELS_ENDPOINT = ApiKeyManager.getApiModelsURL(ApiType.OPENAI);
    private static final Credential apiCredentials = ApiKeyManager.getApiCredentials(ApiType.OPENAI);


    @Override
    public List<ModelInfo> fetchAvailableModels() throws GenAIApiException {
        String user = apiCredentials.getUser();
        String password = apiCredentials.getPassword();
        if(user == null ||  user.trim().isEmpty() ){
            throw new IllegalStateException("OPEANAI_USER is not set");
        }
        if(password == null || password.trim().isEmpty()){
            throw new IllegalStateException("OPEANAI_PASSWORD is not set");
        }
        String accessToken = GenAiApiClient.getAccessToken(user, password);
        return GenAiApiClient.getSupportedModelsWithId(accessToken);
        /*List<ModelInfo> models = new ArrayList<>();

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            models.add(new ModelInfo("API key not found", ""));
            return models;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");

                for (JsonElement element : data) {
                    JsonObject modelObj = element.getAsJsonObject();
                    String id = modelObj.get("id").getAsString();

                    // Filter to include only relevant models
                    if (id.startsWith("gpt-") || id.startsWith("text-davinci-")) {
                        models.add(new ModelInfo(id, id));
                    }
                }
            } else {
                models.add(new ModelInfo("Error: " + response.statusCode(), ""));
            }
        } catch (IOException | InterruptedException e) {
            models.add(new ModelInfo("Error fetching models: " + e.getMessage(), ""));
        }

        return models;*/
    }

    @Override
    public String getApiKey() {
        return ApiKeyManager.getApiKey(ApiType.OPENAI);
    }

    @Override
    public GeneralCommandLine getGeneralCommandLine(Project project, String model) {
        String apiKey = getApiKey();
        if(apiKey == null || apiKey.trim().isEmpty()){
            throw new IllegalStateException("openai.api_key not set");
        }

        String apiBase = ApiKeyManager.getApiBase(ApiType.OPENAI);
        if( apiBase == null || apiBase.trim().isEmpty()){
            throw new IllegalStateException("OPENAI_API_BASE not set");
        }

        String aiderCertFilePath = ApiKeyManager.getAiderCertFilePath();
        if( aiderCertFilePath == null || aiderCertFilePath.trim().isEmpty()){
            throw new IllegalStateException("SSL_CERT_FILE not set");
        }
        GeneralCommandLine commandLine = null;

        if(isVirtualEnvEnabled()){
            String aiderVirtualEnvPath = ApiKeyManager.getAiderVirtualEnvPath();
            if( aiderVirtualEnvPath == null || aiderVirtualEnvPath.trim().isEmpty()){
                throw new IllegalStateException("ENABLE_VIRTUAL_ENV is enabled but AIDER_VIRTUAL_ENV_PATH not set");
            }
            commandLine = new GeneralCommandLine("/bin/bash");
            commandLine.setWorkDirectory(project.getBasePath());
            commandLine.addParameter("-c");
            commandLine.addParameter("source " + aiderVirtualEnvPath + " && aider --model openai/" + model + " --api-key openai=" + apiKey);
        }else {
            commandLine = new GeneralCommandLine("aider");
            commandLine.setWorkDirectory(project.getBasePath());
            // Add model and API key parameters
            commandLine.addParameter("--model");
            commandLine.addParameter("openai/" + model);
            commandLine.addParameter("--api-key");
            commandLine.addParameter("openai="+apiKey);
        }
        //commandLine.addParameter(apiBase);
        Map<String, String> env = commandLine.getEnvironment();
        env.put("OPENAI_API_BASE", apiBase);
        env.put("SSL_CERT_FILE", aiderCertFilePath);

        return commandLine;
    }

    @Override
    public String chatWithGenAIApi(String model, List<String> impactedClassesWithPrompt)
            throws GenAIApiException {
        String user = apiCredentials.getUser();
        String password = apiCredentials.getPassword();
        if(user == null ||  user.trim().isEmpty() ){
            throw new IllegalStateException("OPEANAI_USER is not set");
        }
        if(password == null || password.trim().isEmpty()){
            throw new IllegalStateException("OPEANAI_PASSWORD is not set");
        }
        return LLMManager.invokeLLM(impactedClassesWithPrompt, user, password, model);
    }
}