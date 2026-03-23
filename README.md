# Hobby AI Concierge

A full-stack AI chatbot with a microservices architecture that routes hobby-related questions to domain-specific expert agents and maintains conversation memory across turns. Built with Spring Boot, LangChain, Docker, and deployed to AWS Elastic Beanstalk.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                     Browser                         │
│           Google Stitch UI (Thymeleaf)              │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP + Session Cookie
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot (Port 8080)                │
│                                                     │
│  ┌─────────────────┐    ┌───────────────────────┐   │
│  │ Spring Security │    │    ChatController     │   │
│  │ Session Auth    │    │    /chat (GET + POST) │   │
│  │ CSRF Protection │    └───────────┬───────────┘   │
│  └─────────────────┘                │               │
│                          ┌──────────▼──────────┐    │
│                          │  LangChainService   │    │
│                          │  WebClient proxy    │    │
│                          └──────────┬──────────┘    │
└─────────────────────────────────────┼───────────────┘
                       Internal HTTP  │ (not public)
┌─────────────────────────────────────▼───────────────┐
│              FastAPI + LangChain (Port 8000)        │
│                                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │           MultiPromptChain Router            │   │
│  └────────────┬─────────────────┬───────────────┘   │
│               │                 │                   │
│  ┌────────────▼──────┐  ┌───────▼────────────────┐  │
│  │   Guitar Chain    │  │  Photography Chain     │  │
│  │  + Buffer Window  │  │     (stateless)        │  │
│  │    Memory (k=2)   │  └────────────────────────┘  │
│  └───────────────────┘                              │
└─────────────────────────────────────────────────────┘
                       │
               ┌───────▼───────┐
               │  OpenAI API   │
               └───────────────┘
```

**How the routing works:**
- The **router** reads the user's input and decides which destination chain best fits the question
- The **guitar chain** uses `ConversationBufferWindowMemory` to remember the last `k` exchanges
- The **photography chain** is stateless — each question is answered independently
- The **default chain** handles anything that doesn't match a specialist

**Key design decisions:**
- Spring Boot is the sole public entry point — FastAPI is only reachable internally
- Authentication is handled at the Spring Boot layer before any request reaches the AI service
- The guitar chain maintains a sliding window memory of the last `k=2` exchanges — questions beyond the window are forgotten, demonstrating controlled context degradation


---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Google Stitch, Thymeleaf, Tailwind CSS |
| API Gateway | Spring Boot 4.0, Spring Security |
| AI Service | Python, FastAPI, LangChain, OpenAI GPT-4o-mini |
| Containerization | Docker, Docker Compose |
| Cloud | AWS Elastic Beanstalk, Amazon ECR |
| Build | Maven, pip |


---

## Project Structure

```
hobby-ai-concierge/
├── docker-compose.yml              # Local development
├── aws-deploy/
│   └── docker-compose.yml          # AWS deployment config
├── springboot-app/
│   ├── src/main/java/com/example/springboot_app/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java     # Spring Security setup
│   │   │   └── MvcConfig.java          # View controller mappings
│   │   ├── controller/
│   │   │   └── ChatController.java     # /chat GET + POST endpoints
│   │   └── service/
│   │       └── LangChainService.java   # WebClient proxy to FastAPI
│   ├── src/main/resources/
│   │   ├── templates/
│   │   │   ├── login.html
│   │   │   ├── home.html
│   │   │   └── chat.html
│   │   └── application.properties
│   └── Dockerfile
└── langchain-service/
    ├── app/
    │   ├── __init__.py
    │   └── main.py                 # FastAPI + LangChain router
    ├── requirements.txt
    └── Dockerfile
```

---
