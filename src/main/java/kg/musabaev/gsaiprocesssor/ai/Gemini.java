package kg.musabaev.seogooglesheetshelper.ai;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kg.musabaev.seogooglesheetshelper.chatgpt.ChatGptResponsePacket;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Gemini {

    private final CloseableHttpClient http;
    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";
    private final Gson gson = new Gson();
    public Gemini() {
        this.http = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();
    }


    public String execute(final String textContent) {
        log.info("Handling Gemini query...");
        var httpPost = new HttpPost(GEMINI_API_URL + System.getProperty("GEMINI_API"));
        String json = getJson(textContent);
        httpPost.addHeader(new BasicHeader("Content-Type", "application/json"));
        log.info("Request body to Gemini API:\n{}", json);
        log.info(Arrays.toString(httpPost.getAllHeaders()));
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = http.execute(httpPost)) {
            var responseBody = EntityUtils.toString(response.getEntity());
            log.info("Got response by Gemini API:\n{}", responseBody);
            var obj = JsonParser.parseString(responseBody);
            return obj
                    .getAsJsonObject().get("candidates")
                    .getAsJsonArray().get(0)
                    .getAsJsonObject().get("content")
                    .getAsJsonObject().get("parts")
                    .getAsJsonArray().get(0)
                    .getAsJsonObject().get("text")
                    .getAsString();
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private static String getJson(String textContent) {
        textContent = textContent.replace("\"", "'");
        String query = """
                Look, you will be my SEO assistant and you will help me
                compose the meta title and meta description. Naturally, observe the total length and try not to exceed the length. For example, the description length should be between 150 and 190 characters\s
                Use more keywords, not numbers,
                Write everything in the usual sentence format
                I will give you the content in Russian language, and you have to give me the result in Russian in only one
                format without unnecessary spaces, explanations, descriptions, whitespaces and the like. Always add '| Новости Kompanion' (in russian!!!) to the end of the meta title\s
                So first comes the meta title and meta description, and there must be a '[mid]' symbol between them, that is, there must be a slash.\s
                This is mandatory, and then I'm going to format your output in a special way. Is this the content of the page:
                %n%s""".formatted(textContent);
        return """
                        {
                            "contents": [
                                {
                                    "parts": [
                                        {
                                            "text": "%s"
                                        }
                                    ]
                                }
                            ]
                        }
                """.formatted(query);
    }

    public static Map<String, Object> jsonToMap(String jsonString) {
        // Create a Gson object
        Gson gson = new Gson();

        // Parse the JSON string into a JsonElement
        JsonElement jsonElement = JsonParser.parseString(jsonString);

        // Check if the JsonElement is a JsonObject
        if (jsonElement.isJsonObject()) {
            // Cast the JsonElement to a JsonObject
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Create a HashMap to store the data
            Map<String, Object> map = new HashMap<>();

            // Iterate over the JsonObject entries
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                // Get the key and value
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                // Get the value based on its type
                if (value.isJsonPrimitive()) {
                    // Handle primitive types (String, Number, Boolean)
                    map.put(key, value.getAsString());
                } else if (value.isJsonObject()) {
                    // Handle nested objects
                    map.put(key, gson.fromJson(value, Map.class));
                } else if (value.isJsonArray()) {
                    // Handle arrays
                    map.put(key, gson.fromJson(value, Object[].class));
                }
            }

            // Return the map
            return map;
        }
        // Return null if the JSON string is not a valid JsonObject
        return Collections.emptyMap();
    }
}
