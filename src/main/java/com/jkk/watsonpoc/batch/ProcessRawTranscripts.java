package com.jkk.watsonpoc.batch;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Convert the results of the Watson speech-to-text service to clean document transcripts
 */
public class ProcessRawTranscripts {
    public static void main(String[] args) throws IOException {
        Path inputFolder = Paths.get("audio/transcripts");
        Gson gson = new Gson();
        Files.list(inputFolder).forEach((path) -> {
            try {
                // Read the raw JSON transcript results from the Watson speech-to-text API
                String rawContents = new String(Files.readAllBytes(path));

                // Process the results into a single, clean text string
                SpeechResults speechResults = gson.fromJson(rawContents, SpeechResults.class);
                StringBuilder cleanTranscript = new StringBuilder();
                for (Transcript transcript : speechResults.getResults()) {
                    String line = transcript.getAlternatives().get(0).getTranscript();
                    line = line.replace("%HESITATION ", ""); // remove unneeded annotations
                    cleanTranscript.append(line);
                }

                // Write the result out to an output file
                Path outputFile = Paths.get("audio/transcripts-clean/" + path.getFileName().toString().split("\\.")[0] + ".txt");
                Files.write(outputFile, cleanTranscript.toString().getBytes());
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });
    }
}
