package com.example.springboot_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LangChainService {
    
    private final WebClient webClient;

    public LangChainService(@Value("${fastapi.url}") String fastapiUrl){
        this.webClient = WebClient.create(fastapiUrl);
    }

    public String chat(String userMessage){
        return webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/chat")
                .queryParam("prompt", userMessage)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
