package io.github.charlotte3517.resumeai.client;

import io.github.charlotte3517.resumeai.exception.AiClientException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class OpenAiClient implements AiClient {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final WebClient webClient;
    private final Duration timeout;

    public OpenAiClient(WebClient webClient, Duration timeout) {
        this.webClient = webClient;
        this.timeout = timeout;
    }

    @Override
    public AiResponse chat(List<ChatMessage> messages) {
        List<OpenAiMessage> openAiMessages = messages.stream()
                .map(m -> new OpenAiMessage(m.role(), m.content()))
                .toList();

        OpenAiRequest request = new OpenAiRequest(DEFAULT_MODEL, openAiMessages);

        OpenAiResponse response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        res -> res.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(
                                        new AiClientException("OpenAI error: " + body)
                                ))
                )
                .bodyToMono(OpenAiResponse.class)
                .timeout(timeout)
                .onErrorMap(
                        ex -> !(ex instanceof AiClientException),
                        ex -> new AiClientException("Failed to call OpenAI API", ex)
                )
                .block();

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiClientException("Empty response from OpenAI");
        }

        String content = response.choices().get(0).message().content();
        int tokensUsed = response.usage() != null ? response.usage().totalTokens() : 0;

        return new AiResponse(content, tokensUsed);
    }
}