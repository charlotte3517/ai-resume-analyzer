package io.github.charlotte3517.resumeai.client;

import java.util.List;

public interface AiClient {
    AiResponse chat(List<ChatMessage> messages);
}
