package com.jkk.watsonpoc.batch;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Call Alchemy API for each of the podcast transcripts
 */
public class AnalyzeTranscriptsWithAlchemy {
    public static void main(String[] args) throws Exception {
        String apiUrl = "http://gateway-a.watsonplatform.net/calls/text/TextGetCombinedData";
        String apiKey = "XXXXXXXXXXXXXXXXXXXXXXX";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("apikey", apiKey);
        params.put("outputMode", "json");
        params.put("extract", "entity,keyword,taxonomy,concept,relation,doc-sentiment,doc-emotion");
        params.put("showSourceText", "1");

        Path inputFolder = Paths.get("audio/transcripts-clean");
        Files.list(inputFolder).forEach((path) -> {
            try {
                String content = new String(Files.readAllBytes(path));
                params.put("text", content);

                // Convert the params into a form-encoded body
                String body = params.entrySet().stream()
                        .map((entry) -> {
                            try {
                                return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .reduce((a, b) -> {
                            return a + "&" + b;
                        })
                        .orElse("");

                // Make the API HTTP request
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                HttpEntity<String> request = new HttpEntity<String>(body, headers);

                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.postForObject(apiUrl, request, String.class);

                // Output the results to text file
                Files.write(Paths.get("audio/transcripts-analysis/" + path.getFileName().toString().split("\\.")[0] + ".json"), result.getBytes());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
