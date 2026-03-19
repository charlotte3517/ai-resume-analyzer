package io.github.charlotte3517.resumeai.client;

import java.util.List;

public record OpenAiRequest(String model, List<OpenAiMessage> messages) {}
