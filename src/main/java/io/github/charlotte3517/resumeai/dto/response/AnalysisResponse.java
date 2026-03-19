package io.github.charlotte3517.resumeai.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID resumeId,
        String content,
        Integer tokensUsed,
        LocalDateTime createdAt
) {}
