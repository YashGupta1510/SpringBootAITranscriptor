package com.nagarro.makenotesopenai.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nagarro.makenotesopenai.service.AIService;

@RestController
@RequestMapping("/api")
public class AIController {
	
	
	@Autowired
	AIService ai;
	
	
	@PostMapping(path = "/analyze", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	ResponseEntity<Object> analyze(@RequestPart MultipartFile audio){
		System.out.println("Trying path for " + audio.getOriginalFilename());
		
		String fileName = audio.getOriginalFilename();
	    File newFile = new File("src/main/resources/audio/" + fileName);
	    InputStream inputStream = null;
	    OutputStream outputStream = null;
		try {
			inputStream = audio.getInputStream();
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			outputStream = new FileOutputStream(newFile);
			int read = 0;
			byte[] bytes = new byte[1024];
		    while ((read = inputStream.read(bytes)) != -1) {
		    	outputStream.write(bytes, 0, read);
		    }
		    
		} catch (IOException e) {
		     e.printStackTrace();
		}
		
		return ai.analyze(fileName);
	}
	
}

