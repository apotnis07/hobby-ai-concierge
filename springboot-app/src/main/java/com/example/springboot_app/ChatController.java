package com.example.springboot_app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
public class ChatController {
    
    private final LangChainService langChainService;

    public ChatController(LangChainService langChainService){
        this.langChainService = langChainService;
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/chat")
    @ResponseBody
    public String sendMessage(@RequestParam String message) {        
        return langChainService.chat(message);
    }
}
