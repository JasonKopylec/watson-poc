package com.jkk.watsonpoc.batch;

import com.google.gson.GsonBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bulk download mp3 podcasts from BBC for use as input corpus
 */
public class DownloadBBCMp3 {
    public static void main(String[] args) throws IOException {
        String bbcUrl = "http://www.bbc.co.uk/programmes/p02tb8vq/episodes/downloads";

        RestTemplate restTemplate = new RestTemplate();
        String bbcMarkup = restTemplate.getForObject(bbcUrl, String.class);

        List<Map<String, String>> resultList = new ArrayList<>();

        // Parse the BBC markup and scrape the podcast info and MP3 urls
        Pattern pattern = Pattern.compile("<span property=\"name\">(.*?)</span>.*?property=\"url\" resource=\"(.*?)\".*?datatype=\"xsd:dateTime\" content=\"(.*?)T.*?buttons__download__link\" href=\"(.*?)\"");
        Matcher matcher = pattern.matcher(bbcMarkup);
        int count = 0;
        while (matcher.find()) {
            Map<String, String> podcastInfo = new LinkedHashMap<>();
            podcastInfo.put("id", "" + count++);
            podcastInfo.put("title", matcher.group(3) + " - " + matcher.group(1));
            podcastInfo.put("content-url", matcher.group(2));
            podcastInfo.put("mp3-url", matcher.group(4));
            resultList.add(podcastInfo);
        }

        // Save podcast metadata to disk for use in the app
        String podCastMetadata = new GsonBuilder().setPrettyPrinting().create().toJson(resultList);
        Files.write(Paths.get("src/main/resources/podcast-info.json"), podCastMetadata.getBytes());

        // Download MP3s to audio/original
        for (Map<String, String> podCastInfo : resultList) {
            String urlString = podCastInfo.get("mp3-url");
            URL website = new URL(urlString);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream("audio/original/" + podCastInfo.get("id") + ".mp3");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }
}
