package com.nagarro.makenotesopenai.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;


public class Transcript {

	private final static String URL = "https://api.openai.com/v1/audio/transcriptions";
    public final static long MAX_ALLOWED_SIZE = 25 * 1024 * 1024;
    public final static long MAX_CHUNK_SIZE_BYTES = 20 * 1024 * 1024;

    private final static String KEY = "OPENAI-API-KEY"; 
    
    private final static String MODEL= "whisper-1";
    
    public static final String WORD_LIST = String.join(", ",
            List.of("Yash", "GPT-3", "GPT-4", "DALL-E",
                    "Midjourney", "AssertJ", "Mockito", "JUnit", "Java", "Kotlin", "Groovy", "Scala",
                    "IOException", "RuntimeException", "UncheckedIOException", "UnsupportedAudioFileException",
                    "assertThrows", "assertTrue", "assertEquals", "assertNull", "assertNotNull", "assertThat",
                    "Tales from the jar side", "Spring Boot", "Spring Framework", "Spring Data", "Spring Security"));

    
    private static String transcribeAudio(String prompt, File file) {
    	System.out.println("Transcribing in private fxn "+ file.getName());
    	
    	try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader("Authorization", "Bearer %s".formatted(KEY));

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setContentType(ContentType.MULTIPART_FORM_DATA)
                    .addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY))
                    .addPart("model", new StringBody(MODEL, ContentType.DEFAULT_TEXT))
                    .addPart("response_format", new StringBody("text", ContentType.DEFAULT_TEXT))
                    .addPart("prompt", new StringBody(prompt, ContentType.DEFAULT_TEXT))
                    .build();
            httpPost.setEntity(entity);
            return client.execute(httpPost, response -> {
                System.out.println("Status: " + new StatusLine(response));
                return EntityUtils.toString(response.getEntity());
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static String transcribe(String fileName) {
        System.out.println("Transcribing the called fxn " + fileName);
        File file = new File(fileName);

        String prompt = WORD_LIST;
        String transcription = "";
        if (file.length() <= MAX_ALLOWED_SIZE) {
           transcription = transcribeAudio(prompt, file);
        } else {
           System.out.println("SIze exceeded");
        }
        String fileNameWithoutPath = fileName.substring(
                fileName.lastIndexOf("\\") + 1);
        
        writeTextToFile(transcription,
        		fileNameWithoutPath.replace(".wav", ".txt"));
        return transcription;
    }
    
    public static void writeTextToFile(String textData, String fileName) {
    	System.out.println("Writing file");
        Path directory = Paths.get("src/main/resources/text");
        Path filePath = directory.resolve(fileName);
        try {
            Files.deleteIfExists(filePath);
            Files.writeString(filePath, textData, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing text to file", e);
        }
    }
    
}
