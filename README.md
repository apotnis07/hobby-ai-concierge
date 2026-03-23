# Hobby AI Concierge

A full-stack AI chatbot with a microservices architecture that routes hobby-related questions to domain-specific expert agents and maintains conversation memory across turns. Built with Spring Boot, LangChain, Docker, and deployed to AWS Elastic Beanstalk.

## Architecture

### Auth Flow

```mermaid
sequenceDiagram
    autonumber
    participant Browser
    participant SpringBoot as Spring Boot

    Browser->>SpringBoot: GET /chat
    Note right of Browser: User navigates to chat page
    
    activate SpringBoot
    SpringBoot->>SpringBoot: Spring Security intercepts
    Note right of SpringBoot: Checks session — not authenticated
    deactivate SpringBoot

    SpringBoot-->>Browser: 302 redirect → /login
    Note right of Browser: Sends user to login page

    Browser->>SpringBoot: GET /login
    SpringBoot-->>Browser: 200 login.html
    Note right of Browser: Thymeleaf renders login page

    Browser->>+SpringBoot: POST /login (credentials)
    Note left of SpringBoot: User submits username + password
    
    SpringBoot->>SpringBoot: Spring Security validates
    Note right of SpringBoot: Checks against UserDetailsService

    SpringBoot-->>-Browser: 302 redirect → /chat
    Note right of Browser: Sets session cookie, redirects

    Browser->>+SpringBoot: GET /chat (with session)
    SpringBoot->>SpringBoot: Security: authenticated
    Note right of SpringBoot: Session cookie valid — allow

    SpringBoot-->>-Browser: 200 chat.html
    Note right of Browser: Thymeleaf renders chat page with CSRF token
```

### Chat Flow

```mermaid
sequenceDiagram
    autonumber
    participant B as Browser
    participant SB as Spring Boot
    participant CC as ChatController
    participant LS as LangChainService
    participant FA as FastAPI
    participant OA as OpenAI

    B->>+SB: POST /chat (message=...)
    Note right of B: JS fetch with CSRF token
    
    SB->>SB: Spring Security checks
    Note right of SB: Validates session + CSRF

    SB->>+CC: ChatController.sendMessage()
    CC->>+LS: langChainService.chat(message)
    
    LS->>+FA: POST /chat?prompt=...
    Note right of LS: WebClient builds URL

    FA->>FA: chain.run(prompt)
    Note right of FA: LLMRouterChain classifies

    FA->>+OA: OpenAI API call
    OA-->>-FA: LLM response
    
    FA-->>-LS: {"response": "..."}
    LS-->>-CC: raw JSON string
    CC-->>-SB: return response string
    SB-->>-B: 200 response body
    
    B->>B: JSON.parse + render bubble
    Note left of B: Extracts .response, appends to DOM
```



---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Google Stitch, Thymeleaf, Tailwind CSS, HTML, JavaScript |
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
│   │   │   ├── login.html              # Login Page
│   │   │   ├── home.html               # Home Page
│   │   │   └── chat.html               # Chat Page
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
