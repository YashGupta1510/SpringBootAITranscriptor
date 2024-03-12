package com.nagarro.makenotesopenai.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nagarro.makenotesopenai.util.Transcript;


@Service
public class AIService {
	
	@Autowired
	ChatClient client;
	

	public ResponseEntity<Object> analyze(String fileName) {
		String transcription = getTranscription(fileName.substring(0,fileName.indexOf('.')));
		System.out.println("Transcription done");
		String summary = getResult(transcription);
		return new ResponseEntity<Object>(summary, HttpStatus.OK);
	}
	

    public String getTranscription(String fileName) {
    	System.out.println(fileName);
        Path transcriptionFilePath = Paths.get("src/main/resources/text", fileName + ".txt");
        Path audioFilePath = Paths.get("src/main/resources/audio", fileName + ".wav");

        if (Files.exists(transcriptionFilePath)) {
            try {
                return Files.readString(transcriptionFilePath);
            } catch (IOException e) {
                System.err.println("Error reading transcription file: " + e.getMessage());
            }
        } else {
        	System.out.println("went to transcribe");
            return Transcript.transcribe(audioFilePath.toString());
        }
		return "";
    }
    
    public String getResult(String transcription){
    	
    	String systemPrompt = """
    			You are a highly skilled AI trained in language comprehension and summarization.
                I would like you to read the following text and summarize it into a concise
                abstract paragraph. Aim to retain the most important points, providing a coherent
                and readable summary that could help a person understand the main points of the
                discussion without needing to read the entire text. Please avoid unnecessary
                details or tangential points.
                """;  
    	
    	String systemPrompt2 = """
                Also, You are a proficient AI with a specialty in distilling information into key points.
                Based on the following text, identify and list the main points that were discussed
                or brought up. These should be the most important ideas, findings, or topics that
                are crucial to the essence of the discussion. Your goal is to provide a list that
                someone could read to quickly understand what was talked about. After the first summary IN FORM OF BULLETS.
                """;
    	SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
    	SystemPromptTemplate systemPromptTemplate2 = new SystemPromptTemplate(systemPrompt2);
    	Message systemMessage = systemPromptTemplate.createMessage();
    	Message systemMessage2 = systemPromptTemplate2.createMessage();
    	
    	Message userMessage = new UserMessage(transcription);
    	
    	Prompt prompt = new Prompt(List.of(systemMessage, systemMessage2, userMessage));

    	List<Generation> response = client.call(prompt).getResults();
    	System.out.println(response);
    	return response.get(0).getOutput().getContent().toString();
    }
	
}
