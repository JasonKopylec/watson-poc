package com.jkk.watsonpoc.batch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combine podcast information with results of speech-to-text analysis
 */
public class CombineMetadataAndAnalysis {
    private static Gson gson;

    public static void main(String[] args) throws Exception {
        Path podcastInfoPath = Paths.get("audio/podcast-info/podcast-info.json");
        String podcastInfoJson = new String(Files.readAllBytes(podcastInfoPath));

        gson = new GsonBuilder().setPrettyPrinting().create();

        List<Map<String, Object>> podcastInfo = gson.fromJson(podcastInfoJson, ArrayList.class);

        podcastInfo.stream().forEach((podcast) -> {
            processPodcast(podcast);
        });

        // Ouput as a static .js file for inclusion for use in web app
        String jsContent = "var podcasts = " + gson.toJson(podcastInfo) + ";";
        Files.write(Paths.get("audio/repository/podcasts.js"), jsContent.getBytes());
    }

    private static void processPodcast(Map<String, Object> podcast) {
        try {
            Path analysisPath = Paths.get("audio/transcripts-analysis/" + podcast.get("id") + ".json");
            String analysisJson = new String(Files.readAllBytes(analysisPath));
            Map<String, Object> analysis = gson.fromJson(analysisJson, HashMap.class);
            podcast.put("text", analysis.get("text"));
            analysis.remove("text");
            podcast.put("analysis", analysis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
