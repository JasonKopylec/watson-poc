package com.jkk.watsonpoc.batch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Convert podcast audio files to text transcripts using the Watson Speech-to-Text Java API
 */
public class ConvertSpeechToText {
    private static Log log = LogFactory.getLog(ConvertSpeechToText.class);

    public static void main(String[] args) throws IOException {
        SpeechToText speechToText = new SpeechToText();
        speechToText.setUsernameAndPassword("XXXX", "XXXX");

        Map<String, Object> speechToTextApiParams = new HashMap<String, Object>();
        speechToTextApiParams.put("content_type", "audio/ogg");
        speechToTextApiParams.put("continuous", true); // translates entire file at once

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Path inputFolder = Paths.get("audio/converted");
        Files.list(inputFolder).forEach((path) -> {
            if (Files.exists(path)) return; // bypass any that have already been processed previously

            log.info("Processing " + path);

            // Run the Watson speech-to-text api
            speechToTextApiParams.put("audio", path.toFile());
            SpeechResults results = speechToText.recognize(speechToTextApiParams);

            // Store the result to disk
            Path outputFile = Paths.get("audio/transcripts/" + path.getFileName().toString().split("\\.")[0] + ".json");
            try {
                Files.write(outputFile, gson.toJson(results).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
