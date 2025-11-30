package com.example.notesai;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class AISummarizer {

    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client;

    public AISummarizer() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Increased timeout for longer completions
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public interface SummaryCallback {
        void onSuccess(String summary);
        void onError(String error);
    }

    public void summarizeText(String text, SummaryCallback callback) {
        // Create the JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            // Updated prompt to generate a study guide with TO-DOs and key concepts
            systemMessage.put("content", "You are a helpful study assistant. Create a concise but detailed study guide based on the following text. " +
                    "Structure your response clearly. " +
                    "1. Highlight the most important concepts. " +
                    "2. Include a specific section listing any TO DOs, assignments, or deadlines mentioned. " +
                    "3. Ensure the summary is comprehensive enough to serve as a standalone review tool.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", text);
            messages.put(userMessage);

            jsonBody.put("messages", messages);
        } catch (Exception e) {
            callback.onError("Failed to create request: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String summary = jsonResponse.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        callback.onSuccess(summary);
                    } catch (Exception e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }
        });
    }
}